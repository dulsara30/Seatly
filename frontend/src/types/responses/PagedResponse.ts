// common/payload/PageDto.java; currentPage is zero-based.
export interface PagedResponse<T> {
  items: T[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}
