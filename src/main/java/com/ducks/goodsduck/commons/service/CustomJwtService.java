package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomJwtService implements JwtService {
    //TODO: 환경변수로 빼야함 (은닉)
    private static final String SECRET_KEY = "aasjjkjaskjdl1kqweqwwrwqedqwdwqdwqdw2naskjkdakj34c8sa";

    @Override
    public String createToken(String subject, long expireTime, JwtDto jwtDto) {
        if (expireTime <= 0) {
            throw new RuntimeException("Expiry time must be greater than Zero : ["+expireTime+"] ");
        }
        // 토큰을 서명하기 위해 사용해야할 알고리즘 선택
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
                .setExpiration(new Date(System.currentTimeMillis()+expireTime))
                .compact();
    }

    @Override
    public String getSubject(String token) {
        return (String) getPayloads(token).get("sub");
    }

    @Override
    public Map<String, Object> getPayloads(String token) {

        //TODO: token 검증 로직 (null, empty string, not valid, expired)

        return new HashMap<>(
                Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody()
        );

    }
}
