package com.ducks.goodsduck.commons.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/test")
public class TestController {

    @Value(value = "${spring.security.oauth2.client.registration.kakao.client-id}")
    private String dbUsername;

    @GetMapping("/v1")
    public Map<String, Object> getProperties(HttpServletRequest request) {
        final Map<String, Object> map = new HashMap<>();
        map.put("DBUsername", dbUsername);
        return map;
    }
}