package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.AuthorizationNaverDto;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OauthNaverService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String naverOauth2ClientId = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.client-id");
    private final String naverOauth2ClientSecret = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.client-secret");
    private final String frontendRedirectUrl = PropertyUtil.getProperty("spring.security.oauth2.client.registration.naver.redirect-uri"); //

    public AuthorizationNaverDto callTokenApi(String code, String state) {
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

            AuthorizationNaverDto authorization = objectMapper.readValue(response.getBody(), AuthorizationNaverDto.class);

            return authorization;
        } catch (RestClientException | JsonProcessingException ex) {
            ex.printStackTrace();
            return new AuthorizationNaverDto();
        }
    }

    /**
     * accessToken 을 이용한 유저정보 받기
     * @return Json Data(String)
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

            return response.getBody();
        }catch (RestClientException ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            return "Error"; //TODO 어떻게 처리할지
        }
    }
}
