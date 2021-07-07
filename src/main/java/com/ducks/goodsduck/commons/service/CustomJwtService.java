package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CustomJwtService implements JwtService {

    private static final long EXPIRE_TIME = Long.parseLong(PropertyUtil.getProperty("spring.security.jwt.expire-time"));
    private static final String SECRET_KEY = PropertyUtil.getProperty("spring.security.jwt.secret-key");

    @Override
    public String createToken(String subject, JwtDto jwtDto) {
        // 토큰을 서명하기 위해 사용할 알고리즘 선택
        SignatureAlgorithm signatureAlgorithm= SignatureAlgorithm.HS256;

        /* Header 설정 */

        /* Payload 설정 */
        Map<String, Object> payloads = new HashMap<>();
        payloads.put("userId", jwtDto.getUserId());

        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(payloads)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRE_TIME))
                .compact();
    }

    @Override
    public String getSubject(String token) {
        return (String) getPayloads(token).get("sub");
    }

    @Override
    public Map<String, Object> getPayloads(String token) {

        return new HashMap<>(
                Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody()
        );

    }
}
