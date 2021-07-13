package com.ducks.goodsduck.commons.util;

import com.ducks.goodsduck.commons.config.ApplicationContextServe;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PropertyUtil {

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
