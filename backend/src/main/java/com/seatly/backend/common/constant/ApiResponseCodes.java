package com.seatly.backend.common.constant;

/**
 * Status codes as Strings for OpenAPI's @ApiResponse(responseCode = ...),
 * which only accepts a String. Runtime code uses HttpStatus instead.
 */
public final class ApiResponseCodes {

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String BAD_REQUEST = "400";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";

    private ApiResponseCodes() {
    }
}
