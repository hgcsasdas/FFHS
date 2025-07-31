package es.hgccarlos.filehost.controller;

import es.hgccarlos.filehost.dto.CreateBucketRequest;
import es.hgccarlos.filehost.dto.FileDTO;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.dto.SimpleBuketRequest;
import es.hgccarlos.filehost.model.Bucket;
import es.hgccarlos.filehost.repository.BucketRepository;
import es.hgccarlos.filehost.service.BucketService;
import es.hgccarlos.filehost.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/buckets")
@RequiredArgsConstructor
public class BucketController {

    @Autowired BucketService bucketService;


    /**
     * Create a new bucket – returns generated API key
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Response createBucket(@RequestBody CreateBucketRequest req) {
        return bucketService.createBucket(req.getName());
    }

    /**
     * List all buckets – mainly for admin
     */
    @GetMapping
    public Optional<Bucket> listBucket(@RequestBody SimpleBuketRequest req) {
        return bucketService.getBucketByApiKey(req.getApiKey());
    }

    /** List */
    @GetMapping("/all")
    public List<Bucket> listBuckets() {
        return bucketService.listBuckets();
    }

    /** Delete */
    @DeleteMapping()
    public Response delete(@RequestBody SimpleBuketRequest req) {
        return bucketService.deleteBucket(req.getApiKey());
    }

}