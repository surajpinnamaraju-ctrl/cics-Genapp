package com.genapp.dto;

/** Error body preserving the legacy CA-RETURN-CODE contract. */
public record ApiError(String returnCode, String message) {

    public static ApiError of(String returnCode, String message) {
        return new ApiError(returnCode, message);
    }
}
