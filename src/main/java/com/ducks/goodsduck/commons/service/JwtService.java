package com.ducks.goodsduck.commons.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface JwtService {

    String createJwt(String subject, Long userId);
    String getSubject(String token);
    Map<String, Object> getPayloads(String token);
    Map<String, Object> getHeader(String token);
}
