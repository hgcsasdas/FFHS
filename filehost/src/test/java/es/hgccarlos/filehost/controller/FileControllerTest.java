package es.hgccarlos.filehost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.hgccarlos.filehost.config.ApiKeyFilter;
import es.hgccarlos.filehost.config.IpRateLimitFilter;
import es.hgccarlos.filehost.config.JwtAuthFilter;
import es.hgccarlos.filehost.dto.DeleteManyFilesRequest;
import es.hgccarlos.filehost.dto.FileDTO;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.FileEntity;
import es.hgccarlos.filehost.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        IpRateLimitFilter.class,
                        JwtAuthFilter.class,
                        ApiKeyFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "USER")  // cualquier rol válido
class FileControllerTest {

    @Autowired MockMvc mvc;
    @MockBean StorageService service;
    @Autowired ObjectMapper mapper;

    private final String BUCKET = "bucket-123";

    @Test
    @DisplayName("POST /api/files/upload ➞ 200 + Response")
    void uploadSingle() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello".getBytes()
        );

        FileDTO dto = new FileDTO();
        dto.setId(42L);
        dto.setOriginalName("test.txt");
        Response fake = new Response("ok","200","uploaded",null, dto);

        Mockito.when(service.uploadFile(any(), eq(BUCKET))).thenReturn(fake);

        mvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("bucketKey", BUCKET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("uploaded")))
                .andExpect(jsonPath("$.data.id", is(42)))
                .andExpect(jsonPath("$.data.originalName", is("test.txt")));
    }

    @Test
    @DisplayName("POST /api/files/upload-many ➞ 200 + Response")
    void uploadMany() throws Exception {
        MockMultipartFile f1 = new MockMultipartFile("files","a.txt", MediaType.TEXT_PLAIN_VALUE, "a".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("files","b.txt", MediaType.TEXT_PLAIN_VALUE, "b".getBytes());

        FileDTO dto1 = new FileDTO(); dto1.setId(1L); dto1.setOriginalName("a.txt");
        FileDTO dto2 = new FileDTO(); dto2.setId(2L); dto2.setOriginalName("b.txt");
        Response fake = new Response("ok","200","multi",null, List.of(dto1,dto2));

        Mockito.when(service.uploadFiles(any(), eq(BUCKET))).thenReturn(fake);

        mvc.perform(multipart("/api/files/upload-many")
                        .file(f1).file(f2)
                        .param("bucketKey", BUCKET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[1].originalName", is("b.txt")));
    }

    @Test
    @DisplayName("DELETE /api/files/{id} ➞ 200 + Response")
    void deleteOne() throws Exception {
        Response fake = new Response("ok","200","deleted",null,null);
        Mockito.when(service.deleteFile(99L, BUCKET)).thenReturn(fake);

        mvc.perform(delete("/api/files/{id}", 99L)
                        .header("bucketKey", BUCKET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("deleted")));
    }

    @Test
    @DisplayName("DELETE /api/files/delete-many ➞ 200 + Response")
    void deleteMany() throws Exception {
        Response fake = new Response("ok","200","gone",null,null);
        Mockito.when(service.deleteFiles(List.of(5L, 6L).toArray(new Long[0]), BUCKET)).thenReturn(fake);

        mvc.perform(delete("/api/files/delete-many")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new DeleteManyFilesRequest(BUCKET, List.of(5L, 6L).toArray(new Long[0])))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("gone")));
    }

    @Test
    @DisplayName("GET /api/files?bucketKey=… ➞ 200 + List<FileDTO>")
    void listFiles() throws Exception {
        FileDTO f = new FileDTO(); f.setId(7L); f.setOriginalName("foo.pdf");
        Mockito.when(service.listFiles(BUCKET)).thenReturn(List.of(f));

        mvc.perform(get("/api/files").param("bucketKey", BUCKET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(7)))
                .andExpect(jsonPath("$[0].originalName", is("foo.pdf")));
    }

    @Test
    @DisplayName("GET /api/files/view/{id} ➞ 200 + bytes + mime")
    void viewFile() throws Exception {
        byte[] bytes = "imgdata".getBytes(StandardCharsets.UTF_8);
        FileEntity meta = new FileEntity();
        meta.setMimeType(MediaType.IMAGE_PNG_VALUE);
        Mockito.when(service.getFileMetaById(12L, BUCKET)).thenReturn(meta);
        Mockito.when(service.getFileBytes(12L, BUCKET)).thenReturn(bytes);

        mvc.perform(get("/api/files/view/{id}", 12L).header("X-API-KEY", BUCKET))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("GET /api/files/download/{id} ➞ 200 + attachment header")
    void downloadFile() throws Exception {
        byte[] data = {0x01,0x02};
        FileEntity meta = new FileEntity();
        meta.setOriginalName("doc.txt");
        Mockito.when(service.getFileMetaById(33L, BUCKET)).thenReturn(meta);
        Mockito.when(service.getFileBytes(33L, BUCKET)).thenReturn(data);

        mvc.perform(get("/api/files/download/{id}", 33L).header("X-API-KEY", BUCKET))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("doc.txt")))
                .andExpect(content().bytes(data));
    }
}
