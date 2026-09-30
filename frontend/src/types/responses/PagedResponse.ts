/** common/payload/PageDto.java - currentPage is zero-based, like the backend's `page` param. */
export interface PagedResponse<T> {
  items: T[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}
