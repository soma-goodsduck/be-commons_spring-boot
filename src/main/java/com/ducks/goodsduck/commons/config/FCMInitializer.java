package com.ducks.goodsduck.commons.config;

import com.ducks.goodsduck.commons.util.PropertyUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Slf4j
public class FCMInitializer {

    private final String FIREBASE_CONFIG_PATH = PropertyUtil.getProperty("firebase.config-path");
    private final String FIREBASE_DATABASE_URL = PropertyUtil.getProperty("firebase.database-url");

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream()
                    ))
                    .setDatabaseUrl(FIREBASE_DATABASE_URL)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
