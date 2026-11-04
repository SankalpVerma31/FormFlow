package com.formaccess.dto;

import com.formaccess.entity.enums.SummaryType;
import com.formaccess.entity.enums.Visibility;

/** One row of the "which statistics are public?" settings table. */
public class SummaryOption {
    private final SummaryType type;
    private final Long questionId;          // null for event-level summaries
    private final String questionText;
    private final Visibility visibility;

    public SummaryOption(SummaryType type, Long questionId, String questionText, Visibility visibility) {
        this.type = type;
        this.questionId = questionId;
        this.questionText = questionText;
        this.visibility = visibility;
    }

    /** Name of the form field, e.g. "vis_AVERAGE_8" or "vis_RESPONSE_COUNT_0". */
    public String getFieldName() { return "vis_" + type.name() + "_" + (questionId == null ? 0 : questionId); }

    public SummaryType getType() { return type; }
    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public Visibility getVisibility() { return visibility; }
}
