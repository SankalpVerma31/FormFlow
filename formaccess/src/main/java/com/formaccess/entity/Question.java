package com.formaccess.entity;

import com.formaccess.entity.enums.QuestionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Maps to table "questions". Validation rules are stored as columns. */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_value", precision = 15, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 15, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "min_date")
    private LocalDate minDate;

    @Column(name = "max_date")
    private LocalDate maxDate;

    @Column(name = "rating_max")
    private Integer ratingMax;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<QuestionOption> options = new ArrayList<>();

    /** Min/max as plain text for display, e.g. 1.0000 -> "1". */
    public String getMinValueText() { return minValue == null ? null : minValue.stripTrailingZeros().toPlainString(); }
    public String getMaxValueText() { return maxValue == null ? null : maxValue.stripTrailingZeros().toPlainString(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public LocalDate getMinDate() { return minDate; }
    public void setMinDate(LocalDate minDate) { this.minDate = minDate; }
    public LocalDate getMaxDate() { return maxDate; }
    public void setMaxDate(LocalDate maxDate) { this.maxDate = maxDate; }
    public Integer getRatingMax() { return ratingMax; }
    public void setRatingMax(Integer ratingMax) { this.ratingMax = ratingMax; }
    public List<QuestionOption> getOptions() { return options; }
}
