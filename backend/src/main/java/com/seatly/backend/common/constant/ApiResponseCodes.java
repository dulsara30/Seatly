package com.seatly.backend.common.constant;

// Strings because @ApiResponse(responseCode = ...) only accepts a String.
public final class ApiResponseCodes {

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String CONFLICT = "409";

    private ApiResponseCodes() {
    }
}
