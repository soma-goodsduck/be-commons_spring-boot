package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationNaverDto;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.URLEncoder;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OauthNaverService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();

    private final String naverOauth2ClientId = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.naver.client-id", "local");
    private final String naverOauth2ClientSecret = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.naver.client-secret", "local");
    private final String frontendRedirectUrl = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.naver.redirect-uri", "local");
    private final String grantType = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.naver.authorization-grant-type", "local");
    private final String tokenUri = jsonOfAwsSecrets.optString("spring.security.oauth2.client.provider.naver.token-uri", "local");
    private final String userInfoUri = jsonOfAwsSecrets.optString("spring.security.oauth2.client.provider.naver.user-info-uri", "local");

    public AuthorizationNaverDto callTokenApi(String code, String state) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", naverOauth2ClientId);
        params.add("client_secret", naverOauth2ClientSecret);
        params.add("redirect_uri", URLEncoder.DEFAULT.encode(frontendRedirectUrl, StandardCharsets.UTF_8));
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
            return objectMapper.readValue(response.getBody(), AuthorizationNaverDto.class);

        } catch (RestClientException | JsonProcessingException ex) {
            log.debug("exception occured in request to authorize with Naver : {}", ex.getMessage(), ex);
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

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUri, request, String.class);
            return response.getBody();
        } catch (RestClientException ex) {
            log.debug("exception occured in getting access token with Naver : {}", ex.getMessage(), ex);
            return "request not available"; //
        }
    }
}
