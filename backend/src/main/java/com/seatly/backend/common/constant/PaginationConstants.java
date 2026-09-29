package com.seatly.backend.common.constant;

public final class PaginationConstants {

    /**
     * Upper bound on any page request. Also sizes batch fetching of lazy
     * collections, so one page never needs more than one extra query per
     * collection.
     */
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationConstants() {
    }
}
