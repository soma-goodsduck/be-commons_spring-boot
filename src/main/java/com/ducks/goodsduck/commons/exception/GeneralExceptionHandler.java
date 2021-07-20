package com.ducks.goodsduck.commons.exception;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.NoResultException;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@ControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

    private ResponseEntity<ApiResult<?>> newResponse(Throwable throwable, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(ERROR(throwable, status), headers, status);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<?>> handleNotFoundException(Exception e) {
        return newResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<?>> handleMethodNotAllowedException(Exception e) {
        return newResponse(e, HttpStatus.METHOD_NOT_ALLOWED);
    }


    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ApiResult<?>> handleUnauthorizedException(Exception e) {
        log.debug("Unauthorized exception occured: {}", e.getMessage(), e);
        return newResponse(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({NoResultException.class, DuplicateRequestException.class,
                        IllegalArgumentException.class, IllegalStateException.class,
                        MultipartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResult<?>> handleInvalidInputDataException(Exception e) {
        log.debug("Bad request exception occured: {}", e.getMessage(), e);
        return newResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ApiResult<?>> handleUnexpectedException(Exception e) {
        log.debug("Unexpected exception occured: {}", e.getMessage(), e);
        return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
