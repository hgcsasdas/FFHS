package es.hgccarlos.filehost.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeleteManyFilesRequest {

    private String bucketKey;
    private Long[] ids;

}

