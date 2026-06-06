package com.resumainer.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void constructor_withItemsAndPagination_calculatesTotalPages() {
        List<String> items = List.of("a", "b", "c");
        PagedResponse<String> response = new PagedResponse<>(items, 0, 10, 25);

        assertEquals(items, response.getItems());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
    }

    @Test
    void totalPages_whenSizeIsZero_returnsZero() {
        PagedResponse<String> response = new PagedResponse<>(List.of(), 0, 0, 0);

        assertEquals(0, response.getTotalPages());
    }

    @Test
    void totalPages_whenExactDivision_returnsCorrectCount() {
        PagedResponse<String> response = new PagedResponse<>(List.of(), 0, 10, 30);

        assertEquals(3, response.getTotalPages());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        PagedResponse<String> response = new PagedResponse<>();
        response.setItems(List.of("x"));
        response.setPage(1);
        response.setSize(20);
        response.setTotalElements(100);
        response.setTotalPages(5);

        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(100, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
    }
}
