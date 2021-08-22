package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.service.CustomJwtService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.annotation.NoCheckJwt;
import com.ducks.goodsduck.commons.model.dto.ApiResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jwt")
public class JwtController {

    private final CustomJwtService jwtService;
    private final UserService userService;

    // 개발 테스트용 (토큰 발급)
    @NoCheckJwt
    @GetMapping("/gen/token")
    public ApiResult<Map<String, Object>> genToken(@RequestParam(value="subject") String subject) {
        String token = jwtService.createJwt(subject, 2L);
        Map<String, Object> map = new HashMap<>();
        map.put("result", token);
        return OK(map);
    }

    @NoCheckJwt
    @GetMapping("/get/subject")
    public ApiResult<Map<String, Object>> getSubject(@RequestHeader("jwt") String token) {
        String subject = jwtService.getSubject(token);
        Map<String, Object> map = new HashMap<>();
        map.put("result", subject);
        return OK(map);
    }

    @NoCheckJwt
    @GetMapping("/get/payloads")
    public ApiResult<Map<String, Object>> getPayloads(@RequestHeader("jwt") String token) {
        return OK(jwtService.getPayloads(token));
    }

    @NoCheckJwt
    @GetMapping("/get/claims")
    public ApiResult<Jws<Claims>> getClaims(@RequestHeader("jwt") String token) {
        Jws<Claims> claims = jwtService.getClaims(token);
        return OK(claims);
    }
}
