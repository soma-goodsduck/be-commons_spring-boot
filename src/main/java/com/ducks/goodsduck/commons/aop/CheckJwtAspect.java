package com.ducks.goodsduck.commons.aop;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.service.JwtService;
import com.ducks.goodsduck.commons.service.UserService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckJwtAspect {

    private final JwtService jwtService;
    private final UserService userService;

    @Around("execution(* com.ducks.goodsduck.commons.controller.*.*(..))" +
            "&& !@annotation(com.ducks.goodsduck.commons.annotation.NoCheckJwt)")
    public Object validateUserFromJwt(ProceedingJoinPoint joinPoint) throws Throwable {

        // HINT: 메서드 실행 전
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String jwt = request.getHeader("jwt");
        Long userId = userService.checkLoginStatus(jwt);

        // HINT: jwt의 payloads를 통해 userId를 읽었을 경우 UnAuthorized 에러 반환
//        if (userId.equals(-1L)) {
//            return ApiResult.ERROR("There is no jwt or not be able to get payloads.", HttpStatus.UNAUTHORIZED);
//        }

        request.setAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS, userId);

        Object result = joinPoint.proceed();

        // HINT: 본 메서드 실행 후
        HttpServletResponse response = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getResponse();
        response.setHeader("jwt", jwtService.createJwt(PropertyUtil.SUBJECT_OF_JWT, userId));

        return result;
    }
}