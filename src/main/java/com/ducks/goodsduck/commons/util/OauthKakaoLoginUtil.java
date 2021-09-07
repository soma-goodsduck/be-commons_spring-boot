package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.model.dto.oauth2.AuthorizationKakaoDto;
import com.ducks.goodsduck.commons.util.AwsSecretsManagerUtil;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.URLEncoder;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
public class OauthKakaoLoginUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final JSONObject jsonOfAwsSecrets = AwsSecretsManagerUtil.getSecret();

    private static String kakaoOauth2ClientId = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.kakao.client-id", "local");
    private static String kakaoOauth2ClientSecret = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.kakao.client-secret", "local");
    private static String frontendRedirectUrl = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.kakao.redirect-uri", "local");
    private static String grantType = jsonOfAwsSecrets.optString("spring.security.oauth2.client.registration.kakao.authorization-grant-type", "local");
    private static String tokenUri = jsonOfAwsSecrets.optString("spring.security.oauth2.client.provider.kakao.token-uri", "local");
    private static String userInfoUri = jsonOfAwsSecrets.optString("spring.security.oauth2.client.provider.kakao.user-info-uri", "local");

    public static AuthorizationKakaoDto callAccessToken(String code) {

        // HINT: 로컬 환경에서의 환경 변수 설정
        if (jsonOfAwsSecrets.isEmpty()) {
            kakaoOauth2ClientId = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.client-id");
            kakaoOauth2ClientSecret = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.client-secret");
            frontendRedirectUrl = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.redirect-uri");
            grantType = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.authorization-grant-type");
            tokenUri = PropertyUtil.getProperty("spring.security.oauth2.client.provider.kakao.token-uri");
            userInfoUri = PropertyUtil.getProperty("spring.security.oauth2.client.provider.kakao.user-info-uri");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", kakaoOauth2ClientId);
        params.add("client_secret", kakaoOauth2ClientSecret);
        params.add("redirect_uri", URLEncoder.DEFAULT.encode(frontendRedirectUrl, StandardCharsets.UTF_8));
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
            log.debug("Result of getting access token from Kakao: \n" + response.toString());
            return objectMapper.readValue(response.getBody(), AuthorizationKakaoDto.class);
        } catch (RestClientException | JsonProcessingException ex) {
            log.debug("exception occured in request to authorize with Kakao : {}", ex.getMessage(), ex);
            throw new IllegalStateException();
        }
    }

    /**
     * accessToken 을 이용한 유저정보 받기
     * @return Json Data(String)
     */
    public static String callUserInfoByAccessToken(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(userInfoUri, request, String.class);
            log.debug("Result of user information from Kakao: \n" + response.toString());
            return response.getBody();
        } catch (RestClientException ex) {
            log.debug("exception occured in getting access token with Kakao : {}", ex.getMessage(), ex);
            throw new IllegalStateException();
        }
    }
}