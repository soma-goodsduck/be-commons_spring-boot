package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.model.dto.oauth2.PublicKeyOfApple;
import com.ducks.goodsduck.commons.model.dto.oauth2.PublicKeysOfApple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class OauthAppleLoginUtil {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<PublicKeyOfApple> callPublicToken() {
        try {
            ResponseEntity<PublicKeysOfApple> response = restTemplate.getForEntity("https://appleid.apple.com/auth/keys", PublicKeysOfApple.class);
            log.debug("Result of getting access token from Apple: \n" + response.toString());
            return response.getBody().getKeys();
        } catch (RestClientException ex) {
            log.debug("exception occured in request to authorize with Kakao : {}", ex.getMessage(), ex);
            throw new IllegalStateException();
        }
    }
}
