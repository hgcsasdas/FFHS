package es.hgccarlos.filehost.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class FileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "relative_path", nullable = false)
    private String relativePath;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private String hash;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime = LocalDateTime.now();

    @Column(name = "bucket_id", nullable = false)
    private Long bucketId;

}