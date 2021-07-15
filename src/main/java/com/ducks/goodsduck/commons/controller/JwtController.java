package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.JwtDto;
import com.ducks.goodsduck.commons.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/jwt")
public class JwtController {

    @Autowired
    private JwtService jwtService;

    // 개발 테스트용 (토큰 발급)
    @GetMapping("/gen/token")
    public Map<String, Object> genToken(@RequestParam(value="subject") String subject) {
        String token = jwtService.createJwt(subject, new JwtDto(10L));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("result", token);
        return map;
    }

    @GetMapping("/get/subject")
    public Map<String, Object> getSubject(@RequestHeader("jwt") String jwt) {
        String subject = jwtService.getSubject(jwt);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("result", subject);
        return map;
    }

    @GetMapping("/get/payloads")
    public Map<String, Object> getPayloads(@RequestHeader("jwt") String jwt) {
        return jwtService.getPayloads(jwt);
    }
}
