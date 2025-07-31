package es.hgccarlos.filehost.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    String status;
    String code;
    String message;
    String description;
    Object data;

}
