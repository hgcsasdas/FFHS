package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.FileDTO;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.Bucket;
import es.hgccarlos.filehost.model.FileEntity;
import es.hgccarlos.filehost.repository.FileRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StorageServiceImpl implements StorageService {

    private final FileRepository fileRepo;
    private final BucketService bucketService;
    private final MeterRegistry meterRegistry;

    @Value("${fileEntity.upload-dir}")
    private String uploadDir;

    // ---- Upload Single File ----
    @Override
    public Response uploadFile(MultipartFile file, String bucketKey) {

        Bucket bucket = bucketService.getBucketByApiKey(bucketKey)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        Long bucketId = bucket.getId();
        String bucketPath = bucket.getPath();

        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            String hash = calculateHash(file);
            if (fileRepo.findByHashAndBucketId(hash, bucketId).isPresent()) {
                meterRegistry.counter("file.upload.duplicate",
                        "bucketId", bucketId.toString()).increment();
                return new Response("error", "FILE_DUPLICATED",
                        "File already exists in this bucket", file.getOriginalFilename(), null);
            }

            // store on disk
            String ext = getExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path targetDir = Paths.get(uploadDir, bucketPath);
            Files.createDirectories(targetDir);
            Path dest = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            // persist metadata
            FileEntity e = new FileEntity();
            e.setOriginalName(file.getOriginalFilename());
            e.setStoredName(storedName);
            e.setRelativePath(bucketPath + "/" + storedName);
            e.setMimeType(file.getContentType());
            e.setSizeBytes(file.getSize());
            e.setUploadTime(LocalDateTime.now());
            e.setHash(hash);
            e.setBucketId(bucketId);
            fileRepo.save(e);

            // metrics on success
            meterRegistry.counter("file.upload.success",
                    "bucketId", bucketId.toString()).increment();
            meterRegistry.summary("file.upload.size",
                    "bucketId", bucketId.toString()).record(file.getSize());

            return new Response("success", "201",
                    "File uploaded successfully", null, toDTO(e));

        } catch (Exception ex) {
            meterRegistry.counter("file.upload.fail",
                    "bucketId", bucketId.toString()).increment();
            log.error("uploadFile: unexpected error", ex);
            return new Response("error", "UPLOAD_FAIL",
                    "Could not upload file", ex.getMessage(), null);
        } finally {
            timer.stop(Timer.builder("file.upload.time")
                    .description("Time to upload a file")
                    .tags("bucketId", bucketId.toString())
                    .register(meterRegistry));
        }
    }

    // ---- Upload Multiple Files ----
    @Override
    public Response uploadFiles(MultipartFile[] files, String bucketKey) {
        Counter multiCounter = meterRegistry.counter("file.upload.batch",
                "bucketId", bucketService.getBucketByApiKey(bucketKey)
                        .orElseThrow().getId().toString());
        multiCounter.increment(files.length);

        List<FileDTO> uploaded = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        for (MultipartFile f : files) {
            Response r = uploadFile(f, bucketKey);
            if ("success".equals(r.getStatus())) {
                uploaded.add((FileDTO) r.getData());
            } else if ("FILE_DUPLICATED".equals(r.getCode())) {
                duplicates.add(f.getOriginalFilename());
            }
        }
        String status = duplicates.isEmpty() ? "success" : "partial";
        String code   = duplicates.isEmpty() ? "201" : "207";
        String desc   = duplicates.isEmpty() ? null
                : "Duplicate files: " + String.join(", ", duplicates);
        return new Response(status, code,
                "Multi-file upload complete", desc, uploaded);
    }

    // ---- List Files in Bucket ----
    @Override
    public List<FileDTO> listFiles(String bucketKey) {
        Bucket bucket = bucketService.getBucketByApiKey(bucketKey)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        Long bucketId = bucket.getId();
        meterRegistry.counter("file.list.count", "bucketId", bucketId.toString()).increment();
        return fileRepo.findAllByBucketId(bucketId)
                .stream().map(this::toDTO).toList();
    }

    // ---- Get File Metadata by ID ----
    @Override
    public FileEntity getFileMetaById(Long id, String bucketKey) {
        Bucket bucket = bucketService.getBucketByApiKey(bucketKey)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        meterRegistry.counter("file.meta.request", "bucketId", bucket.getId().toString()).increment();
        FileEntity entity = fileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if (!entity.getBucketId().equals(bucket.getId())) {
            meterRegistry.counter("file.meta.accessDenied",
                    "bucketId", bucket.getId().toString()).increment();
            throw new RuntimeException("File does not belong to this bucket");
        }
        return entity;
    }

    // ---- Get Raw Bytes of a File ----
    @Override
    public byte[] getFileBytes(Long id, String bucketKey) throws IOException {
        FileEntity entity = getFileMetaById(id, bucketKey);
        meterRegistry.counter("file.download.count",
                "bucketId", entity.getBucketId().toString()).increment();
        Path path = Paths.get(uploadDir, entity.getRelativePath());
        return Files.readAllBytes(path);
    }

    // ---- Delete File ----
    @Override
    public Response deleteFile(Long id, String bucketKey) {
        Bucket bucket = bucketService.getBucketByApiKey(bucketKey)
                .orElseThrow(() -> new RuntimeException("Invalid API key"));
        Long bucketId = bucket.getId();

        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            FileEntity entity = getFileMetaById(id, bucketKey);
            Path path = Paths.get(uploadDir, entity.getRelativePath());
            Files.deleteIfExists(path);
            fileRepo.delete(entity);

            meterRegistry.counter("file.delete.success",
                    "bucketId", bucketId.toString()).increment();

            return new Response("success", "200", "File deleted", null, null);

        } catch (Exception ex) {
            meterRegistry.counter("file.delete.fail",
                    "bucketId", bucketId.toString()).increment();
            log.error("deleteFile: error deleting file id={}", id, ex);
            return new Response("error", "DELETE_FAIL",
                    "Could not delete file", ex.getMessage(), null);
        } finally {
            timer.stop(Timer.builder("file.delete.time")
                    .description("Time to delete a file")
                    .tags("bucketId", bucketId.toString())
                    .register(meterRegistry));
        }
    }

    // ---- Delete Multiple Files ----
    @Override
    public Response deleteFiles(Long[] ids, String bucketKey) {
        Counter batchDel = meterRegistry.counter("file.delete.batch",
                "bucketId", bucketService.getBucketByApiKey(bucketKey)
                        .orElseThrow().getId().toString());
        batchDel.increment(ids.length);

        List<Long> deleted = new ArrayList<>(), failed = new ArrayList<>();
        for (Long id : ids) {
            Response r = deleteFile(id, bucketKey);
            if ("success".equals(r.getStatus())) deleted.add(id);
            else failed.add(id);
        }
        String status = failed.isEmpty() ? "success" : "partial";
        String code   = failed.isEmpty() ? "200" : "207";
        String desc   = failed.isEmpty() ? null
                : "Failed deletions: " + failed;
        return new Response(status, code,
                "Batch delete complete", desc, deleted);
    }

    // ---- Helpers ----
    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1);
    }

    private String calculateHash(MultipartFile file)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(digest);
    }

    private FileDTO toDTO(FileEntity e) {
        return new FileDTO(
                e.getId(),
                e.getOriginalName(),
                e.getStoredName(),
                e.getRelativePath(),
                e.getMimeType(),
                e.getSizeBytes(),
                e.getHash(),
                e.getUploadTime(),
                e.getBucketId()
        );
    }
}
