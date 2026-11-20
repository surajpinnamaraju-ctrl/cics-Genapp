package com.genapp.web;

import com.genapp.dto.ApiError;
import com.genapp.service.GenAppException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

/** Maps exceptions to HTTP statuses while preserving legacy CA-RETURN-CODEs. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GenAppException.class)
    public ResponseEntity<ApiError> handleGenApp(GenAppException ex, HttpServletRequest req) {
        HttpStatus status = switch (ex.getReturnCode()) {
            case "01" -> HttpStatus.NOT_FOUND;
            case "02" -> HttpStatus.CONFLICT;
            case "70" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "98", "99" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(ApiError.of(ex.getReturnCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest().body(ApiError.of("98", "Invalid request data"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("90", "Internal error"));
    }
}
