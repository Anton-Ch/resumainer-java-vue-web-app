package com.resumainer.service;

import com.resumainer.dao.ResumeDao;
import com.resumainer.model.PagedResponse;
import com.resumainer.model.SavedResume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResumeServiceTest {

    private ResumeDao resumeDao;
    private ResumeService resumeService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        resumeDao = mock(ResumeDao.class);
        resumeService = new ResumeService(resumeDao);
        userId = UUID.randomUUID();
    }

    @Test
    void listResumes_withValidParams_returnsPagedResponse() {
        List<SavedResume> items = List.of(new SavedResume(), new SavedResume());
        when(resumeDao.findByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(items);
        when(resumeDao.countByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(25L);

        PagedResponse<SavedResume> result = resumeService.listResumes(
                userId, null, null, null, null, null, null, "createdAt,desc", 0, 10);

        assertEquals(2, result.getItems().size());
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void listResumes_withInvalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, null, null, null, null, null, null, "invalidField,asc", 0, 10));
    }

    @Test
    void listResumes_withNegativePage_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, null, null, null, null, null, null, "createdAt,desc", -1, 10));
    }

    @Test
    void listResumes_withInvalidSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, null, null, null, null, null, null, "createdAt,desc", 0, 5));
    }

    @Test
    void deleteResume_ownedByUser_returnsTrue() {
        when(resumeDao.softDelete(1L, userId)).thenReturn(true);

        boolean result = resumeService.deleteResume(userId, 1L);

        assertTrue(result);
        verify(resumeDao).softDelete(1L, userId);
    }

    @Test
    void deleteResume_notOwned_returnsFalse() {
        when(resumeDao.softDelete(999L, userId)).thenReturn(false);

        boolean result = resumeService.deleteResume(userId, 999L);

        assertFalse(result);
    }
}
