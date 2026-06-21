package com.resumainer.service;

import com.resumainer.dao.PdfRenderConfigDao;
import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.PdfFitLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for PDF render configuration (Feature 008).
 * Wraps PdfRenderConfigDao with error handling for missing config.
 */
@Service
public class PdfRenderConfigService {

    private static final Logger log = LoggerFactory.getLogger(PdfRenderConfigService.class);

    private final PdfRenderConfigDao configDao;

    public PdfRenderConfigService(PdfRenderConfigDao configDao) {
        this.configDao = configDao;
    }

    /** Load active fit limits. Throws if no active config exists. */
    public PdfFitLimits getActiveFitLimits() {
        PdfFitLimits limits = configDao.findActive();
        if (limits == null) {
            throw new IllegalStateException("No active PDF fit limits configuration found. Please seed resume_pdf_fit_limits.");
        }
        log.debug("Loaded active PDF fit limits: key={}, maxAttempts={}", limits.getConfigKey(), limits.getMaxAttempts());
        return limits;
    }

    /** Load all fill targets for the active fit limits. */
    public List<PdfFillTarget> getActiveFillTargets() {
        PdfFitLimits limits = getActiveFitLimits();
        List<PdfFillTarget> targets = configDao.findFillTargets(limits.getId());
        log.debug("Loaded {} fill targets for fit config: {}", targets.size(), limits.getConfigKey());
        return targets;
    }
}
