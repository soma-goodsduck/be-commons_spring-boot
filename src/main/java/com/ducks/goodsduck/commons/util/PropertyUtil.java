package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.config.ApplicationContextServe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class PropertyUtil {

    public static final String SUBJECT_OF_JWT = "For Member-Checking";
    public static final String KEY_OF_USERID_IN_JWT_PAYLOADS = "userId";
    public static final Integer PAGEABLE_SIZE = 5;

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

