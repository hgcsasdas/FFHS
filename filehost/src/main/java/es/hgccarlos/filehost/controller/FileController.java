package es.hgccarlos.filehost.controller;

import es.hgccarlos.filehost.dto.DeleteManyFilesRequest;
import es.hgccarlos.filehost.dto.FileDTO;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.FileEntity;
import es.hgccarlos.filehost.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    @Autowired StorageService service;
    private static final String HEADER = "X-API-KEY";

    /** Single upload */
    @PostMapping("/upload")
    public Response upload(@RequestParam("bucketKey") String bucketKey, @RequestParam("file") MultipartFile file) {
        return service.uploadFile(file, bucketKey);
    }

    /** Multi upload */
    @PostMapping("/upload-many")
    public Response uploadMany( @RequestParam("bucketKey") String bucketKey,
                               @RequestParam("files") MultipartFile[] files) {
        return service.uploadFiles(files, bucketKey);
    }

    /** Delete */
    @DeleteMapping("/{id}")
    public Response delete(@RequestHeader("bucketKey") String bucketKey, @PathVariable Long id) {
        return service.deleteFile(id, bucketKey);
    }

    @DeleteMapping("/delete-many")
    public Response deleteMany (@RequestBody DeleteManyFilesRequest req) {
        return service.deleteFiles(req.getIds(), req.getBucketKey());
    }


    /** List */
    @GetMapping
    public List<FileDTO> list(@RequestParam("bucketKey") String bucketKey) {
        return service.listFiles(bucketKey);
    }

    /** View */
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> view(@RequestHeader(HEADER) String apiKey,
                                       @PathVariable Long id) throws IOException {
        FileEntity meta = service.getFileMetaById(id, apiKey);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getMimeType()))
                .body(service.getFileBytes(id, apiKey));
    }

    /** Download */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@RequestHeader(HEADER) String apiKey,
                                           @PathVariable Long id) throws IOException {
        FileEntity meta = service.getFileMetaById(id, apiKey);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + meta.getOriginalName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.getFileBytes(id, apiKey));
    }
}