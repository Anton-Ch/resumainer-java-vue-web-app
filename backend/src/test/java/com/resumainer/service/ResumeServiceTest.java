package com.resumainer.service;

import com.resumainer.dao.ResumeDao;
import com.resumainer.model.PagedResponse;
import com.resumainer.model.SavedResume;
import com.resumainer.dto.home.HomeSavedResumeDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResumeServiceTest {

    private ResumeDao resumeDao;
    private HomeSavedResumeMapper homeMapper;
    private ResumeService resumeService;
    private HttpServletRequest request;
    private UUID userId;

    @BeforeEach
    void setUp() {
        resumeDao = mock(ResumeDao.class);
        homeMapper = mock(HomeSavedResumeMapper.class);
        request = mock(HttpServletRequest.class);
        resumeService = new ResumeService(resumeDao, homeMapper);
        userId = UUID.randomUUID();
    }

    @Test
    void listResumes_withValidParams_returnsPagedResponse() {
        SavedResume entity1 = new SavedResume();
        entity1.setId(1L);
        SavedResume entity2 = new SavedResume();
        entity2.setId(2L);
        List<SavedResume> entities = List.of(entity1, entity2);

        HomeSavedResumeDto dto1 = new HomeSavedResumeDto();
        dto1.setId(1L);
        HomeSavedResumeDto dto2 = new HomeSavedResumeDto();
        dto2.setId(2L);

        when(resumeDao.findByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10)))
                .thenReturn(entities);
        when(homeMapper.toDto(entity1, request)).thenReturn(dto1);
        when(homeMapper.toDto(entity2, request)).thenReturn(dto2);
        when(resumeDao.countByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(25L);

        PagedResponse<HomeSavedResumeDto> result = resumeService.listResumes(
                userId, request, null, null, null, null, null, null, "createdAt,desc", 0, 10);

        assertEquals(2, result.getItems().size());
        assertEquals(25L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

        // Verify the sort field was correctly translated from createdAt → created_at
        verify(resumeDao).findByUserId(eq(userId), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10));
    }

    @Test
    void listResumes_withDefaultSort_whenSortParamNull_usesCreatedAtDesc() {
        when(resumeDao.findByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10)))
                .thenReturn(List.of());
        when(resumeDao.countByUserId(any(), any(), any(), any(), any(), any(), any())).thenReturn(0L);

        resumeService.listResumes(userId, request, null, null, null, null, null, null, null, 0, 10);

        verify(resumeDao).findByUserId(eq(userId), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10));
    }

    @Test
    void listResumes_withDefaultSort_whenSortParamEmpty_usesCreatedAtDesc() {
        when(resumeDao.findByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10)))
                .thenReturn(List.of());
        when(resumeDao.countByUserId(any(), any(), any(), any(), any(), any(), any())).thenReturn(0L);

        resumeService.listResumes(userId, request, null, null, null, null, null, null, "", 0, 10);

        verify(resumeDao).findByUserId(eq(userId), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(10));
    }

    @Test
    void listResumes_withInvalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, request, null, null, null, null, null, null, "invalidField,asc", 0, 10));
    }

    @Test
    void listResumes_withNegativePage_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, request, null, null, null, null, null, null, "createdAt,desc", -1, 10));
    }

    @Test
    void listResumes_withInvalidSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeService.listResumes(userId, request, null, null, null, null, null, null, "createdAt,desc", 0, 5));
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
