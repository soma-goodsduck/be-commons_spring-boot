package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.domain.AuthorizationNaver;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.URLEncoder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class OauthNaverService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String naverOauth2ClientId = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.client-id");
    private final String naverOauth2ClientSecret = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.client-secret");
    private final String frontendRedirectUrl = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.redirect-uri"); //

    public AuthorizationNaver callTokenApi(String code, String state) {
        String grantType = "authorization_code";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", naverOauth2ClientId);
        params.add("client_secret", naverOauth2ClientSecret);
        params.add("redirect_uri", URLEncoder.DEFAULT.encode(frontendRedirectUrl, StandardCharsets.UTF_8)); //
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String url = "https://nid.naver.com/oauth2.0/token";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            AuthorizationNaver authorization = objectMapper.readValue(response.getBody(), AuthorizationNaver.class);
            System.out.println("authorization.getAccess_token() = " + authorization.getAccess_token());

            return authorization;
        } catch (RestClientException | JsonProcessingException ex) {
            ex.printStackTrace();
            return new AuthorizationNaver();
        }
    }

    /**
     * accessToken 을 이용한 유저정보 받기
     * @return
     */
    public String callGetUserByAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String url = "https://openapi.naver.com/v1/nid/me";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 값 리턴
            return response.getBody();
        }catch (RestClientException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }
}
