// BucketServiceImpl.java
package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.Bucket;
import es.hgccarlos.filehost.model.FileEntity;
import es.hgccarlos.filehost.repository.BucketRepository;
import es.hgccarlos.filehost.repository.FileRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BucketServiceImpl implements BucketService {

    private final BucketRepository bucketRepo;
    private final FileRepository fileRepo;
    private final MeterRegistry meterRegistry;

    @Value("${fileEntity.upload-dir}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() throws IOException {
        rootPath = Paths.get(uploadDir);
        Files.createDirectories(rootPath);
        log.info("Upload root directory initialized at {}", rootPath);
    }


    // ---- Create Bucket ----
    @Override
    public Response createBucket(String name) {
        Counter.builder("bucket.create.count").register(meterRegistry).increment();
        if (bucketRepo.findByName(name).isPresent()) {
            log.warn("Bucket '{}' already exists", name);
            return new Response("error","BUCKET_EXISTS","Bucket already exists",null,null);
        }
        String apiKey = UUID.randomUUID().toString();
        String path = name;

        Bucket bucket = new Bucket();
        bucket.setName(name);
        bucket.setPath(path);
        bucket.setApiKey(apiKey);
        bucketRepo.save(bucket);

        try {
            Path bucketDir = rootPath.resolve(path);
            Files.createDirectories(bucketDir);
            log.info("Created bucket '{}' at {}", name, bucketDir);
            return new Response("success","201","Bucket created",null, apiKey);
        } catch (IOException e) {
            log.error("Could not create bucket dir for {}", name, e);
            throw new RuntimeException("Bucket directory creation failed", e);
        }
    }


    // ---- Delete Bucket ----
    @Override
    public Response deleteBucket(String apiKey) {
        Counter.builder("bucket.delete.count").register(meterRegistry).increment();
        Bucket bucket = resolveBucket(apiKey);
        String path = bucket.getPath();

        // delete all files
        List<FileEntity> files = fileRepo.findAllByBucketId(bucket.getId());
        files.forEach(f -> {
            try { Files.deleteIfExists(Paths.get(uploadDir, f.getRelativePath())); }
            catch (IOException ex) { log.warn("Could not delete {}", f.getRelativePath(), ex); }
        });
        fileRepo.deleteAll(files);

        // delete directory
        try {
            Path dir = Paths.get(uploadDir, path);
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> { try{Files.deleteIfExists(p);} catch(IOException ex){}} );
            log.info("Deleted bucket directory '{}'", dir);
        } catch (IOException ex) {
            log.warn("Error deleting bucket dir '{}'", path, ex);
        }

        bucketRepo.delete(bucket);
        log.info("Bucket '{}' deleted", bucket.getName());
        return new Response("success","200","Bucket deleted", null, null);
    }

    // ---- Rotate API Key ----
    @Override
    public Response rotateApiKey(String apiKey) {
        Counter.builder("bucket.rotateKey.count").register(meterRegistry).increment();
        Bucket bucket = resolveBucket(apiKey);
        String old = bucket.getApiKey();
        bucket.setApiKey(UUID.randomUUID().toString());
        bucketRepo.save(bucket);
        log.info("Rotated API key for bucket '{}': {} → {}", bucket.getName(), old, bucket.getApiKey());
        return new Response("success","200","API key rotated", null, bucket.getApiKey());
    }

    // ---- List Buckets ----
    @Override
    public List<Bucket> listBuckets() {
        meterRegistry.counter("bucket.list.count").increment();
        return bucketRepo.findAll();
    }

    // ---- Get Bucket by API Key ----
    @Override
    public Optional<Bucket> getBucketByApiKey(String apiKey) {
        meterRegistry.counter("bucket.getByKey.count").increment();
        return bucketRepo.findByApiKey(apiKey);
    }

    private Bucket resolveBucket(String apiKey) {
        return bucketRepo.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Invalid API Key"));
    }


}
