package com.ducks.goodsduck.commons.aop;

import com.ducks.goodsduck.commons.service.JwtService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckJwtAspect {

    private final JwtService jwtService;

    @Before("execution(* com.ducks.goodsduck.commons.controller.*.*(..))")
    public void checkUserIdFromJwt(JoinPoint jp) throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String jwt = request.getHeader("jwt");

        if (jwt != null && !jwt.isBlank()) {
            request.setAttribute("userId", Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS)));
        }

    }
}