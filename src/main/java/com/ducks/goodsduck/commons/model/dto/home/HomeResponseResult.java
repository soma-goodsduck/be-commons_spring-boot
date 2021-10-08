package com.ducks.goodsduck.commons.model.dto.home;

import com.ducks.goodsduck.commons.model.dto.ApiError;
import org.springframework.http.HttpStatus;

public class HomeResponseResult<T, M> {

    private final Boolean success;
    private final Boolean hasNext;
    private final M user;
    private final T response;
    private final ApiError error;

    public HomeResponseResult(Boolean success, Boolean hasNext, M user, T response, ApiError error) {
        this.success = success;
        this.hasNext = hasNext;
        this.user = user;
        this.response = response;
        this.error = error;
    }

    public static <T, M> HomeResponseResult<T, M> OK(Boolean hasNext, M user, T response) {
        return new HomeResponseResult<>(true, hasNext, user, response, null);
    }

    public static HomeResponseResult<?, ?> ERROR(Throwable throwable, HttpStatus status) {
        return new HomeResponseResult<>(false, false, null, null, new ApiError(throwable, status));
    }

    public static HomeResponseResult<?, ?> ERROR(String errorMessage, HttpStatus status) {
        return new HomeResponseResult<>(false, false, null, null, new ApiError(errorMessage, status));
    }

    public Boolean isSuccess() {
        return success;
    }

    public Boolean isHasNext() {
        return hasNext;
    }

    public M getUser() { return user; }

    public T getResponse() {
        return response;
    }

    public ApiError getError() {
        return error;
    }
}
