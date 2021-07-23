package com.ducks.goodsduck.commons.model.dto.item;

import com.ducks.goodsduck.commons.model.dto.ApiError;
import org.springframework.http.HttpStatus;

public class ItemDetailResponseFinal<T> {

    private final Boolean success;
    private final Boolean hasNext;
    private final ItemDetailResponseUser user;
    private final T response;
    private final ApiError error;

    public ItemDetailResponseFinal(Boolean success, Boolean hasNext, ItemDetailResponseUser user, T response, ApiError error) {
        this.success = success;
        this.hasNext = hasNext;
        this.user = user;
        this.response = response;
        this.error = error;
    }

    public static <T> ItemDetailResponseFinal<T> OK(Boolean hasNext, ItemDetailResponseUser itemDetailResponseUser, T response) {
        return new ItemDetailResponseFinal<>(true, hasNext, itemDetailResponseUser, response, null);
    }

    public static ItemDetailResponseFinal<?> ERROR(Throwable throwable, HttpStatus status) {
        return new ItemDetailResponseFinal<>(false, false, null, null, new ApiError(throwable, status));
    }

    public static ItemDetailResponseFinal<?> ERROR(String errorMessage, HttpStatus status) {
        return new ItemDetailResponseFinal<>(false, false, null, null, new ApiError(errorMessage, status));
    }

    public Boolean isSuccess() {
        return success;
    }

    public Boolean isHasNext() {
        return hasNext;
    }

    public ItemDetailResponseUser getUser() {
        return user;
    }

    public T getResponse() {
        return response;
    }

    public ApiError getError() {
        return error;
    }
}
