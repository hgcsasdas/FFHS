package es.hgccarlos.filehost.repository;

import es.hgccarlos.filehost.model.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BucketRepository extends JpaRepository<Bucket, Long> {
    Optional<Bucket> findByApiKey(String apiKey);
    Optional<Bucket> findByName(String name);
}