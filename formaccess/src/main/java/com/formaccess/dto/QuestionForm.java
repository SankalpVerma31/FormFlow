package com.formaccess.dto;

import com.formaccess.entity.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One question typed in the form builder. Options are typed one per line. */
public class QuestionForm {

    @NotBlank(message = "Question text is required")
    @Size(max = 500)
    private String questionText;

    @Size(max = 500)
    private String helpText;

    @NotNull(message = "Choose a type")
    private QuestionType questionType = QuestionType.SHORT_TEXT;

    private boolean required;

    @Size(max = 500)
    private String defaultValue;

    private Integer minLength;
    private Integer maxLength;
    private BigDecimal minValue;
    private BigDecimal maxValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate minDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate maxDate;

    private Integer ratingMax = 5;

    /** For DROPDOWN / RADIO / CHECKBOX: one option per line. */
    private String optionsText;

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String v) { this.questionText = v; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String v) { this.helpText = v; }
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType v) { this.questionType = v; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean v) { this.required = v; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String v) { this.defaultValue = v; }
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer v) { this.minLength = v; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer v) { this.maxLength = v; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal v) { this.minValue = v; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal v) { this.maxValue = v; }
    public LocalDate getMinDate() { return minDate; }
    public void setMinDate(LocalDate v) { this.minDate = v; }
    public LocalDate getMaxDate() { return maxDate; }
    public void setMaxDate(LocalDate v) { this.maxDate = v; }
    public Integer getRatingMax() { return ratingMax; }
    public void setRatingMax(Integer v) { this.ratingMax = v; }
    public String getOptionsText() { return optionsText; }
    public void setOptionsText(String v) { this.optionsText = v; }
}
