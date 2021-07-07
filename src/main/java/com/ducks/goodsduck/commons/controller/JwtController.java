package com.ducks.goodsduck.commons.controller;

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

    @GetMapping("/get/subject")
    public Map<String, Object> getSubject(@RequestParam("token") String token) {
        String subject = jwtService.getSubject(token);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("result", subject);
        return map;
    }

    @GetMapping("/get/payloads")
    public Map<String, Object> getPayloads(@RequestParam("token") String token) {
        return jwtService.getPayloads(token);
    }
}
