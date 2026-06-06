package com.resumainer.model;

import java.util.List;

/**
 * Generic paginated response wrapper for list endpoints.
 * <p>
 * Used by {@code GET /api/resumes} to return a page of saved resumes
 * with pagination metadata.
 *
 * @param <T> the type of items in the page
 */
public class PagedResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedResponse() {
    }

    /**
     * Constructs a fully populated paged response.
     *
     * @param items         the page content
     * @param page          current page number (0-indexed)
     * @param size          page size requested
     * @param totalElements total items across all pages
     */
    public PagedResponse(List<T> items, int page, int size, long totalElements) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (size > 0) ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
