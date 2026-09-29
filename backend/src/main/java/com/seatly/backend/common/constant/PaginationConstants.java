package com.seatly.backend.common.constant;

public final class PaginationConstants {

    // Strings because @RequestParam(defaultValue = ...) only accepts a String.
    public static final String FIRST_PAGE = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";

    public static final int MIN_PAGE_NUMBER = 0;
    public static final int MIN_PAGE_SIZE = 1;

    /**
     * Upper bound on any page request. Also sizes batch fetching of lazy
     * collections, so one page never needs more than one extra query per
     * collection.
     */
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationConstants() {
    }
}
