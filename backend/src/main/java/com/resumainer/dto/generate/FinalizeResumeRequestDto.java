package com.resumainer.dto.generate;

/**
 * Request to finalize a generation request with a selected adaptation level.
 */
public class FinalizeResumeRequestDto {

    private String selectedAdaptationLevel;  // MINIMAL, BALANCED, or MAXIMUM

    public FinalizeResumeRequestDto() {}

    public String getSelectedAdaptationLevel() { return selectedAdaptationLevel; }
    public void setSelectedAdaptationLevel(String selectedAdaptationLevel) { this.selectedAdaptationLevel = selectedAdaptationLevel; }
}
