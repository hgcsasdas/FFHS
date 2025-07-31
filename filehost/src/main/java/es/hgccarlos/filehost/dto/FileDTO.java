package es.hgccarlos.filehost.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {

    private Long id;
    private String originalName;
    private String storedName;
    private String relativePath;
    private String mimeType;
    private Long sizeBytes;
    private String hash;
    private LocalDateTime uploadTime;
    private Long bucketId;

}
