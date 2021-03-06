package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CustomJwtService implements JwtService {

    private final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();
    private final ObjectMapper objectMapper;

    private final String STRING_EXPIRE_TIME = jsonOfAwsSecrets.optString("spring.security.jwt.expire-time", "10000000000");
    private final String SECRET_KEY = jsonOfAwsSecrets.optString("spring.security.jwt.secret-key", "QW76QWORJOQPWNTHOWQN2QWBLK1QWBTKLQQIHR5W7QHWI6WQWBR7KLQWBK4LRQWRQWKNR48QWTOWQ:ORNQWLQ2NRWQ6K3BRKQWORJQOQ");

    public CustomJwtService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Jws<Claims> getClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                .build()
                .parseClaimsJws(jwt);
    }

    public Map<String, Object> getClaimsWithoutSignedKey(String jwt) throws JsonProcessingException {
        String[] charactors = jwt.split("[.]");
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper objectMapper = new ObjectMapper();
        String string = new String(decoder.decode(charactors[0]));

        return objectMapper.readValue(string, Map.class);
    }

    @Override
    public String createJwt(String subject, Long userId) {

        Long EXPIRE_TIME = Long.valueOf(STRING_EXPIRE_TIME);

        // ????????? ???????????? ?????? ????????? ???????????? ??????
        SignatureAlgorithm signatureAlgorithm= SignatureAlgorithm.HS256;

        /* Header ?????? */

        /* Payload ?????? */
        Map<String, Object> payloads = new HashMap<>();
        payloads.put(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS, userId);

        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(payloads)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .compact();
    }

    @Override
    public String getSubject(String jwt) {
        return (String) getPayloads(jwt).get("sub");
    }

    @Override
    public Map<String, Object> getPayloads(String jwt) { return new HashMap<>(getClaims(jwt).getBody()); }

    @Override
    public Map<String, Object> getHeader(String jwt) { return getClaims(jwt).getHeader(); }

    @Override
    public Map<String, Object> getHeaderWithoutSignedKey(String jwt) throws JsonProcessingException {
        return getClaimsWithoutSignedKey(jwt);
    }
}
