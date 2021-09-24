package com.ducks.goodsduck.commons.exception;

import com.ducks.goodsduck.commons.exception.common.*;
import com.ducks.goodsduck.commons.exception.image.ImageProcessException;
import com.ducks.goodsduck.commons.exception.image.InvalidMetadataException;
import com.ducks.goodsduck.commons.exception.user.InvalidJwtException;
import com.ducks.goodsduck.commons.exception.user.InvalidUserRoleException;
import com.ducks.goodsduck.commons.exception.user.Oauth2Exception;
import com.ducks.goodsduck.commons.exception.user.SmsAuthorizationException;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
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

    private ResponseEntity<ApiResult<?>> newApplicationResponse(ApplicationException applicationException, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(ERROR(applicationException), headers, status);
    }

    // HINT: 어플리케이션 내에서 발생하는 예외처리에 사용
    @ExceptionHandler({DuplicatedDataException.class, InvalidJwtException.class,
                        InvalidUserRoleException.class, InvalidStateException.class,
                        ImageProcessException.class, InvalidMetadataException.class,
                        Oauth2Exception.class, SmsAuthorizationException.class,
                        NotFoundDataException.class,})
    public ResponseEntity<ApiResult<?>> handleApplicationException(ApplicationException e) {
        return newApplicationResponse(e, HttpStatus.OK);
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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<?>> handleNotFoundException(Exception e) {
        return newResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidRequestDataException.class, DuplicateRequestException.class,
                       IllegalArgumentException.class, IllegalStateException.class,
                       MultipartException.class, MissingServletRequestParameterException.class,
                       InvalidArgumentException.class})
    public ResponseEntity<ApiResult<?>> handleInvalidInputDataException(Exception e) {
        log.debug("Bad request exception occured: {}", e.getMessage(), e);
        return newResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoResultException.class, CoolsmsException.class})
    public ResponseEntity<ApiResult<?>> handleCoolSmsException(CoolsmsException e) {
        log.debug("Error occured during sending CoolSMS (code: {}): {} ", e.getCode(), e.getMessage(), e);
        return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ResponseEntity<ApiResult<?>> handleUnexpectedException(Exception e) {
        log.debug("Unexpected exception occured: {}", e.getMessage(), e);
        return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
