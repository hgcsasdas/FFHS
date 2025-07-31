package es.hgccarlos.filehost.config;

import es.hgccarlos.filehost.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response> handleRuntime(RuntimeException ex){
        log.error("Runtime error", ex);
        Response body = new Response("error","RUNTIME_EXCEPTION",
                ex.getMessage(), null, null);
        return ResponseEntity.badRequest().body(body);
    }

}
