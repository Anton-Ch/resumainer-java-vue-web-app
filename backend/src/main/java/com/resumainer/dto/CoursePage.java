package com.resumainer.dto;

import com.resumainer.model.CourseCertificate;
import java.util.List;

/**
 * Paginated response DTO for Courses & Certificates.
 * Matches the PrimeVue DataTable lazy pagination format.
 */
public class CoursePage {

    private List<CourseCertificate> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public CoursePage() {
    }

    public CoursePage(List<CourseCertificate> content, long totalElements,
                      int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.number = page;
        this.size = size;
    }

    public List<CourseCertificate> getContent() {
        return content;
    }

    public void setContent(List<CourseCertificate> content) {
        this.content = content;
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
