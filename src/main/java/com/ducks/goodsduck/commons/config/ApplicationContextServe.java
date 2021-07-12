package com.ducks.goodsduck.commons.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * application 설정 파일에서 환경 변수값 받아오기 위한 클래스
 * com.ducks.goodsduck.commons.util.PropertyUtil 에서 사용
 */

@Component
public class ApplicationContextServe implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}