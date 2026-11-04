package com.formaccess.validation;

import com.formaccess.entity.enums.QuestionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for server-side answer validation. */
class AnswerValidatorTest {

    private final AnswerValidator validator = new AnswerValidator();

    private QuestionRules rules(QuestionType type, boolean required) {
        return new QuestionRules("Q", type, required, null, null, null, null, null, null,
                type == QuestionType.RATING ? 5 : null, Set.of(1L, 2L, 3L));
    }

    @Test
    void requiredQuestionCannotBeEmpty() {
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.SHORT_TEXT, true), List.of("  ")));
    }

    @Test
    void optionalQuestionMayBeEmpty() {
        assertTrue(validator.validate(rules(QuestionType.SHORT_TEXT, false), List.of()).isEmpty());
    }

    @Test
    void textLengthLimits() {
        QuestionRules r = new QuestionRules("Q", QuestionType.SHORT_TEXT, true, 3, 5, null, null, null, null, null, Set.of());
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("ab")));
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("abcdef")));
        assertEquals("abcd", validator.validate(r, List.of("abcd")).text());
    }

    @Test
    void integerRangeAndWholeNumber() {
        QuestionRules r = new QuestionRules("Q", QuestionType.INTEGER, true, null, null,
                new BigDecimal("1"), new BigDecimal("4"), null, null, null, Set.of());
        assertEquals(3, validator.validate(r, List.of("3")).number().intValue());
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("5")));
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("2.5")));
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("abc")));
    }

    @Test
    void emailFormat() {
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.EMAIL, true), List.of("not-an-email")));
        assertEquals("a@b.com", validator.validate(rules(QuestionType.EMAIL, true), List.of("a@b.com")).text());
    }

    @Test
    void dateRestrictions() {
        QuestionRules r = new QuestionRules("Q", QuestionType.DATE, true, null, null, null, null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, Set.of());
        assertThrows(AnswerValidationException.class, () -> validator.validate(r, List.of("2025-12-31")));
        assertEquals("2026-06-15", validator.validate(r, List.of("2026-06-15")).text());
    }

    @Test
    void ratingMustBeOnScale() {
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.RATING, true), List.of("6")));
        assertEquals(4, validator.validate(rules(QuestionType.RATING, true), List.of("4")).number().intValue());
    }

    @Test
    void yesNo() {
        assertEquals("YES", validator.validate(rules(QuestionType.YES_NO, true), List.of("yes")).text());
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.YES_NO, true), List.of("maybe")));
    }

    @Test
    void choiceMustBelongToQuestion() {
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.RADIO, true), List.of("99")));
        assertThrows(AnswerValidationException.class, () -> validator.validate(rules(QuestionType.RADIO, true), List.of("1", "2")));
        assertEquals(List.of(1L, 3L), validator.validate(rules(QuestionType.CHECKBOX, true), List.of("1", "3", "1")).optionIds());
    }

    @Test
    void timeIsNormalised() {
        assertEquals("14:30", validator.validate(rules(QuestionType.TIME, true), List.of("14:30")).text());
    }
}
