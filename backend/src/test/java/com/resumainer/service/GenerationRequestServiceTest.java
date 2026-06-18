package com.resumainer.service;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dto.generate.GenerationRequestCreateDto;
import com.resumainer.model.AiModel;
import com.resumainer.model.ResumeGenerationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for GenerationRequestService.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerationRequestServiceTest {

    @Mock
    private GenerationRequestDao requestDao;

    @Mock
    private AiModelDao aiModelDao;

    private GenerationRequestService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID aiModelId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GenerationRequestService(requestDao, aiModelDao);
    }

    @Test
    void createRequest_withValidModel_createsAndReturnsRequest() {
        AiModel model = new AiModel();
        model.setId(aiModelId);
        when(aiModelDao.findById(aiModelId)).thenReturn(model);

        GenerationRequestCreateDto dto = createValidDto();
        ResumeGenerationRequest created = new ResumeGenerationRequest();
        created.setId(requestId);
        created.setUserId(userId);
        when(requestDao.create(any())).thenReturn(created);

        ResumeGenerationRequest result = service.createRequest(userId, dto);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(aiModelDao).findById(aiModelId);
        verify(requestDao).create(any());
    }

    @Test
    void createRequest_withNonExistentModel_throwsIllegalArgument() {
        when(aiModelDao.findById(aiModelId)).thenReturn(null);

        GenerationRequestCreateDto dto = createValidDto();

        assertThrows(IllegalArgumentException.class,
                () -> service.createRequest(userId, dto));
        verify(requestDao, never()).create(any());
    }

    @Test
    void findById_delegatesToDao() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        ResumeGenerationRequest result = service.findById(requestId, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(requestDao).findById(requestId, userId);
    }

    @Test
    void findById_returnsNull_whenNotFound() {
        when(requestDao.findById(requestId, userId)).thenReturn(null);

        assertNull(service.findById(requestId, userId));
    }

    @Test
    void updateSettings_withValidPendingRequest_updatesSuccessfully() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setStatus("pending");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                aiModelId, true)).thenReturn(true);

        service.updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                aiModelId, true);

        verify(requestDao).updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                aiModelId, true);
    }

    @Test
    void updateSettings_withNullRequest_throwsIllegalArgument() {
        when(requestDao.findById(requestId, userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                        aiModelId, true));
        verify(requestDao, never()).updateSettings(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void updateSettings_withNonPendingRequest_throwsIllegalArgument() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setStatus("processing");
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                        aiModelId, true));
        verify(requestDao, never()).updateSettings(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void updateSettings_whenDaoUpdateFails_throwsIllegalArgument() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setStatus("pending");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.updateSettings(any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateSettings(requestId, userId, "ENGLISH_ONLY", "BALANCED",
                        aiModelId, true));
    }

    private GenerationRequestCreateDto createValidDto() {
        GenerationRequestCreateDto dto = new GenerationRequestCreateDto();
        dto.setAiModelId(aiModelId);
        dto.setVacancyTitle("Software Engineer");
        dto.setVacancyDescription("Build cool stuff");
        dto.setCompanyName("Tech Corp");
        dto.setCompanyDescription("A tech company");
        dto.setAdditionalComments("Remote");
        dto.setIncludeCoverLetter(true);
        dto.setLanguageMode("ENGLISH_ONLY");
        dto.setAdaptationSelection("BALANCED");
        return dto;
    }
}
