package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.config.ApplicationContextServe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyUtil {

    public static final String SUBJECT_OF_JWT = "For Member-Checking";
    public static final String KEY_OF_USERID_IN_JWT_PAYLOADS = "userId";
    public static final Integer PAGEABLE_SIZE = 20;
    public static final Integer POST_PAGEABLE_SIZE = 10;
    public static final String BASIC_IMAGE_URL = "https://goodsduck-s3.s3.ap-northeast-2.amazonaws.com/sample_goodsduck.png";

    public static String getProperty(String propertyName) {

        var applicationContext = ApplicationContextServe.getApplicationContext();

        if (applicationContext != null && applicationContext.getEnvironment().getProperty(propertyName) != null) {
            return applicationContext.getEnvironment().getProperty(propertyName);
        } else if (applicationContext == null) {
            log.debug("applicationContext was not loaded!");
        } else if (applicationContext.getEnvironment().getProperty(propertyName) == null) {
            log.debug(propertyName + " properties was not loaded!");
        }
        return "No Value";
    }
}

