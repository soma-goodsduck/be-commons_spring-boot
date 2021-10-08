package com.ducks.goodsduck.commons.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface JwtService {

    String createJwt(String subject, Long userId);
    String getSubject(String token);
    Map<String, Object> getPayloads(String token);
    Map<String, Object> getHeader(String token);
    Map<String, Object> getHeaderWithoutSignedKey(String token) throws JsonProcessingException;
}
