package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import org.json.JSONObject;
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

//    @Value(value = "${test}")
    private String dbUsername;

    @GetMapping("/v1")
    public Map<String, Object> getProperties(HttpServletRequest request) {
        dbUsername = "test";
        final Map<String, Object> map = new HashMap<>();
//        map.put("AwsSecrets", AwsSecretsManagerUtil.getSecret());
        JSONObject jsonObject = new JSONObject("{\"test\":\"hell\"}");
        map.put("test_please",jsonObject.get("test"));
        map.put("DBUsername", dbUsername);
        return map;
    }
}