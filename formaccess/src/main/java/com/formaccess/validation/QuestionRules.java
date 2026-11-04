package com.formaccess.validation;

import com.formaccess.entity.enums.QuestionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** The validation settings of one question, copied out of the Question entity. */
public record QuestionRules(String questionText,
                            QuestionType type,
                            boolean required,
                            Integer minLength,
                            Integer maxLength,
                            BigDecimal minValue,
                            BigDecimal maxValue,
                            LocalDate minDate,
                            LocalDate maxDate,
                            Integer ratingMax,
                            Set<Long> allowedOptionIds) {
}
