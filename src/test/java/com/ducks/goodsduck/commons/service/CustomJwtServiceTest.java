package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.config.location=" +
        "classpath:/application.yml," +
        "classpath:/application-db.yml," +
        "classpath:/application-oauth2.yml")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomJwtServiceTest {

    private long EXPIRE_TIME;
    private String SECRET_KEY;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private String sub = "test";
    private String userId = "1L";
    private String jwt;

    @Autowired private JwtService jwtService;

    @BeforeAll
    void setUp() {
        EXPIRE_TIME = Long.parseLong(String.valueOf(PropertyUtil.getProperty("spring.security.jwt.expire-time")));
        SECRET_KEY = PropertyUtil.getProperty("spring.security.jwt.secret-key");
        jwt = jwtService.createJwt(sub, new JwtDto(1L));
    }

    /** 토큰 생성 관련 */
    @Test
    void 토큰은_3개의_파트로_이루어져야_한다() {
        String[] split = jwt.split("\\.");
        assertThat(split.length).isEqualTo(3);
    }

    @Test
    void 토큰의_헤더에는_HS256_알고리즘이_포함되어야_한다() {
        Map<String, Object> header = jwtService.getHeader(jwt);
        assertThat(header.get("alg")).isEqualTo(signatureAlgorithm.getValue());
    }

    @Test
    void JWT_페이로드의_sub는_생성할_때_입력한_sub여야_한다() {
        assertThat(jwtService.getSubject(jwt)).isEqualTo(sub);
    }

    /** 토큰 복호화 관련 */
    @Test
    void 토큰은_다른_SECRET_KET로는_복호화_불가능_해야_한다() {
        final String TEST_SECRET_KEY = "pwojropwqjrpowqjrpoweopqwjrpowqjropwqjprwqjoprwjqoprjwqoprjwqoprqqoprqorjwqprjor2rmwpfqwporjopqr32wjqrq";

        // JwtException 발생해야 한다.
        assertThatThrownBy(() ->
            {
                Map<String, Object> testPayload = new HashMap<>(Jwts.parserBuilder()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(TEST_SECRET_KEY))
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody());
            }
        ).isInstanceOf(JwtException.class);
    }

    @Test
    void 토큰은_생성_시_입력한_USERID_정보를_포함해야_한다() {
        Map<String, Object> payloads = jwtService.getPayloads(jwt);
        assertThat(payloads.containsKey("userId")).isNotNull();
    }

    @Test
    void 토큰의_페이로드가_변형되면_복호화_불가능_해야_한다() {
        //given
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());


        //when
        Map<String, Object> payloads = jwtService.getPayloads(jwt);
        payloads.put("testKey", "testValue");

        // JWT 만드는 코드
        String changedToken = Jwts.builder()
                .setSubject(sub)
                .addClaims(payloads)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .compact();

        String[] partOfJwt = jwt.split("\\.");
        String[] partOfChangedToken = changedToken.split("\\.");

        // 발급 받은 토큰과 payload 부분만 다른 토큰
        String fakeToken = String.join(".", new String[]{partOfJwt[0], partOfChangedToken[1], partOfJwt[2]});

        //then
        assertThatThrownBy(() -> jwtService.getPayloads(fakeToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 토큰의_서명이_변형되면_복호화_불가능_해야_한다() {
        final String TEST_SECRET_KEY = "pwojropwqjrpowqjrpoweopqwjrpowqjropwqjprwqjoprwjqoprjwqoprjwqoprqqoprqorjwqprjor2rmwpfqwporjopqr32wjqrq";

        //given
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(TEST_SECRET_KEY);
        Key signingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());


        //when
        Map<String, Object> payloads = jwtService.getPayloads(jwt);

        // JWT 만드는 코드
        String changedToken = Jwts.builder()
                .setSubject(sub)
                .addClaims(payloads)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .compact();

        //then
        assertThatThrownBy(() -> jwtService.getPayloads(changedToken))
                .isInstanceOf(JwtException.class);

    }
}