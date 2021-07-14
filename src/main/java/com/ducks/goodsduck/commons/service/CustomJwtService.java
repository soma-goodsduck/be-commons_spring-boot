package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

    private final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();

    private final String stringExpireTime = jsonOfAwsSecrets.optString("spring.security.jwt.expire-time", "10000");
    private final String secretKey = jsonOfAwsSecrets.optString("spring.security.jwt.secret-key", "local");

    public Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
                        .build()
                        .parseClaimsJws(token);
    }

    @Override
    public String createJwt(String subject, JwtDto jwtDto) {

        Long expireTime = Long.valueOf(stringExpireTime);

        // 토큰을 서명하기 위해 사용할 알고리즘 선택
        var signatureAlgorithm= SignatureAlgorithm.HS256;

        /* Header 설정 */

        /* Payload 설정 */
        Map<String, Object> payloads = new HashMap<>();
        payloads.put("userId", jwtDto.getUserId());

        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKey);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(payloads)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .compact();
    }

    @Override
    public Map<String, Object> getPayloads(String token) {
        return new HashMap<>(
                getClaims(token)
                .getBody()
        );
    }

    @Override
    public Map<String, Object> getHeader(String token) {
        return getClaims(token)
                .getHeader();
    }

    @Override
    public String getSubject(String token) {
        return (String) getPayloads(token).get("sub");
    }
}
