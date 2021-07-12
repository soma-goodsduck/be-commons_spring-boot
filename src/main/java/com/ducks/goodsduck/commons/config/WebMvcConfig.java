package com.ducks.goodsduck.commons.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean getFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(new AccessControlAllowFilter());
        registrationBean.setOrder(Integer.MIN_VALUE);
        //registrationBean.addUrlPatterns("/*");
        registrationBean.setUrlPatterns(Arrays.asList("/api/v1/*"));
        return registrationBean;
    }
}
