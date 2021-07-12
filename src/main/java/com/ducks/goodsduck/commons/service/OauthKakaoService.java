package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.AuthorizationKakaoDto;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OauthKakaoService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String kakaoOauth2ClinetId = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.client-id");
    private final String frontendRedirectUrl = PropertyUtil.getProperty("spring.security.oauth2.client.registration.kakao.redirect-uri");

    public AuthorizationKakaoDto callTokenApi(String code) {
        String grantType = "authorization_code";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", grantType);
        params.add("client_id", kakaoOauth2ClinetId);
        params.add("redirect_uri", frontendRedirectUrl + "/auth/kakao/callback");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        String url = "https://kauth.kakao.com/oauth/token";

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            AuthorizationKakaoDto authorization = objectMapper.readValue(response.getBody(), AuthorizationKakaoDto.class);
            return authorization;

        } catch (RestClientException | JsonProcessingException ex) {
            ex.printStackTrace();
//            throw new ProcyanException(E00001);
            throw new IllegalStateException();
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

        String url = "https://kapi.kakao.com/v2/user/me";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            // 값 리턴
            return response.getBody();
        }catch (RestClientException ex) {
            ex.printStackTrace();
//            throw new ProcyanException(E00002);
            throw new IllegalStateException();
        }
    }
}