package es.hgccarlos.filehost.repository;

import es.hgccarlos.filehost.model.Bucket;
import es.hgccarlos.filehost.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByHashAndBucketId(String hash, Long bucketId);
    List<FileEntity> findAllByBucketId(Long id);
}
