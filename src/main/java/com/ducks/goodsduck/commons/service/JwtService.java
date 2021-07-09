package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface JwtService {
    String createToken(String subject, JwtDto jwtDto);
    String getSubject(String token);
    Map<String, Object> getHeader(String token);
    Map<String, Object> getPayloads(String token);
    String getSignature(String token);
    Jws<Claims> getClaims(String token);
}
