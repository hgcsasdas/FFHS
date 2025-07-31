package es.hgccarlos.filehost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.hgccarlos.filehost.dto.CreateBucketRequest;
import es.hgccarlos.filehost.dto.SimpleBuketRequest;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.Bucket;
import es.hgccarlos.filehost.service.BucketService;
import es.hgccarlos.filehost.config.IpRateLimitFilter;
import es.hgccarlos.filehost.config.JwtAuthFilter;
import es.hgccarlos.filehost.config.ApiKeyFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BucketController.class,
        excludeFilters = {
                // quitamos de este slice todos los filtros que son @Component
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        IpRateLimitFilter.class,
                        JwtAuthFilter.class,
                        ApiKeyFilter.class
                })
        }
)
@AutoConfigureMockMvc(addFilters = false)  // desactiva cualquier filtro registrado
class BucketControllerTest {

    @Autowired MockMvc mvc;
    @MockBean BucketService bucketService;
    @Autowired ObjectMapper mapper;

    private final String ADMIN = "admin";

    @Test
    @DisplayName("POST /api/buckets/create ➞ 201 + API key")
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void createBucket() throws Exception {
        Response fake = new Response("ok","200","created",null,"new-api-key");
        Mockito.when(bucketService.createBucket("myBucket"))
                .thenReturn(fake);

        mvc.perform(post("/api/buckets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CreateBucketRequest("myBucket"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data", is("new-api-key")));
    }

    @Test
    @DisplayName("GET /api/buckets (by API key) ➞ 200 + Bucket")
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void getBucketByApiKey() throws Exception {
        Bucket b = new Bucket(1L,"test","/path","key-123",null);
        Mockito.when(bucketService.getBucketByApiKey("key-123"))
                .thenReturn(Optional.of(b));

        mvc.perform(get("/api/buckets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new SimpleBuketRequest("ignored","key-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("test")))
                .andExpect(jsonPath("$.apiKey", is("key-123")));
    }

    @Test
    @DisplayName("GET /api/buckets/all ➞ 200 + list")
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void listBuckets() throws Exception {
        var b1 = new Bucket(1L,"a","p","k",null);
        var b2 = new Bucket(2L,"b","p2","k2",null);
        Mockito.when(bucketService.listBuckets()).thenReturn(List.of(b1,b2));

        mvc.perform(get("/api/buckets/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("DELETE /api/buckets ➞ 200 + message")
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void deleteBucket() throws Exception {
        Response fake = new Response("ok","200","deleted",null,null);
        Mockito.when(bucketService.deleteBucket("del-key"))
                .thenReturn(fake);

        mvc.perform(delete("/api/buckets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new SimpleBuketRequest("ignored","del-key"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("deleted")));
    }
}
