package com.seatly.backend.common.payload;

import java.util.List;

public record PageDto<T>(List<T> items, int currentPage, int totalPages, long totalItems) {
}
