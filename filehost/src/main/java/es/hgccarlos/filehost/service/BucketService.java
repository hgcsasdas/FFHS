package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.Bucket;

import java.util.List;
import java.util.Optional;

public interface BucketService {
    Response createBucket(String name);
    Response deleteBucket(String apiKey);
    Response rotateApiKey(String apiKey);
    List<Bucket> listBuckets();
    Optional<Bucket> getBucketByApiKey(String apiKey);
}

