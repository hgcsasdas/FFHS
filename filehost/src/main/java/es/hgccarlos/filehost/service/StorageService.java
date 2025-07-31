package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.FileDTO;
import es.hgccarlos.filehost.dto.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StorageService {
    Response uploadFile(MultipartFile file, String bucketKey);
    Response uploadFiles(MultipartFile[] files, String bucketKey);
    Response deleteFile(Long id, String bucketKey);
    Response deleteFiles(Long[] ids, String bucketKey);
    List<FileDTO> listFiles(String bucketKey);
    byte[] getFileBytes(Long id, String bucketKey) throws IOException;
    es.hgccarlos.filehost.model.FileEntity getFileMetaById(Long id, String bucketKey);
}