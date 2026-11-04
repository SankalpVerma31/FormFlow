package com.formaccess.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SERVER-SIDE ANSWER VALIDATION (pure Java).
 * The browser also checks inputs, but anyone can bypass the browser, so every
 * answer is checked again here before it is saved.
 */
public class AnswerValidator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int DEFAULT_SHORT_MAX = 500;
    private static final int DEFAULT_LONG_MAX = 5000;

    /**
     * @param rules     the question's settings
     * @param rawValues what the browser sent (several values only for checkboxes)
     * @return the cleaned answer
     * @throws AnswerValidationException with a user-friendly message
     */
    public ValidatedAnswer validate(QuestionRules rules, List<String> rawValues) {
        List<String> values = new ArrayList<>();
        if (rawValues != null) {
            for (String v : rawValues) {
                if (v != null && !v.trim().isEmpty()) values.add(v.trim());
            }
        }

        if (values.isEmpty()) {
            if (rules.required()) throw new AnswerValidationException("This question is required.");
            return ValidatedAnswer.empty();
        }
        if (values.size() > 1 && rules.type() != com.formaccess.entity.enums.QuestionType.CHECKBOX) {
            throw new AnswerValidationException("Only one answer is allowed.");
        }
        String value = values.get(0);

        return switch (rules.type()) {
            case SHORT_TEXT -> new ValidatedAnswer(checkLength(rules, value, DEFAULT_SHORT_MAX), null, List.of());
            case LONG_TEXT -> new ValidatedAnswer(checkLength(rules, value, DEFAULT_LONG_MAX), null, List.of());
            case EMAIL -> {
                if (!EMAIL.matcher(value).matches()) throw new AnswerValidationException("Enter a valid email address.");
                yield new ValidatedAnswer(checkLength(rules, value, 150), null, List.of());
            }
            case INTEGER -> {
                BigDecimal n = parseNumber(value);
                if (n.stripTrailingZeros().scale() > 0) throw new AnswerValidationException("Enter a whole number.");
                yield new ValidatedAnswer(null, checkRange(rules, n), List.of());
            }
            case DECIMAL -> new ValidatedAnswer(null, checkRange(rules, parseNumber(value)), List.of());
            case RATING -> {
                BigDecimal n = parseNumber(value);
                int max = rules.ratingMax() == null ? 5 : rules.ratingMax();
                if (n.stripTrailingZeros().scale() > 0 || n.intValue() < 1 || n.intValue() > max) {
                    throw new AnswerValidationException("Choose a rating from 1 to " + max + ".");
                }
                yield new ValidatedAnswer(null, n, List.of());
            }
            case DATE -> {
                LocalDate d;
                try { d = LocalDate.parse(value); }
                catch (DateTimeParseException e) { throw new AnswerValidationException("Enter a valid date."); }
                if (rules.minDate() != null && d.isBefore(rules.minDate()))
                    throw new AnswerValidationException("Date must be on or after " + rules.minDate() + ".");
                if (rules.maxDate() != null && d.isAfter(rules.maxDate()))
                    throw new AnswerValidationException("Date must be on or before " + rules.maxDate() + ".");
                yield new ValidatedAnswer(d.toString(), null, List.of());
            }
            case TIME -> {
                LocalTime t;
                try { t = LocalTime.parse(value); }
                catch (DateTimeParseException e) { throw new AnswerValidationException("Enter a valid time."); }
                yield new ValidatedAnswer(t.withSecond(0).withNano(0).toString(), null, List.of());
            }
            case YES_NO -> {
                String yn = value.toUpperCase();
                if (!yn.equals("YES") && !yn.equals("NO")) throw new AnswerValidationException("Choose Yes or No.");
                yield new ValidatedAnswer(yn, null, List.of());
            }
            case DROPDOWN, RADIO, CHECKBOX -> new ValidatedAnswer(null, null, checkOptions(rules, values));
        };
    }

    private String checkLength(QuestionRules rules, String value, int defaultMax) {
        int max = rules.maxLength() == null ? defaultMax : rules.maxLength();
        if (rules.minLength() != null && value.length() < rules.minLength())
            throw new AnswerValidationException("Must be at least " + rules.minLength() + " characters.");
        if (value.length() > max)
            throw new AnswerValidationException("Must be at most " + max + " characters.");
        return value;
    }

    private BigDecimal parseNumber(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AnswerValidationException("Enter a valid number.");
        }
    }

    private BigDecimal checkRange(QuestionRules rules, BigDecimal n) {
        if (rules.minValue() != null && n.compareTo(rules.minValue()) < 0)
            throw new AnswerValidationException("Must be at least " + rules.minValue().stripTrailingZeros().toPlainString() + ".");
        if (rules.maxValue() != null && n.compareTo(rules.maxValue()) > 0)
            throw new AnswerValidationException("Must be at most " + rules.maxValue().stripTrailingZeros().toPlainString() + ".");
        return n;
    }

    private List<Long> checkOptions(QuestionRules rules, List<String> values) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (String v : values) {
            long id;
            try { id = Long.parseLong(v); }
            catch (NumberFormatException e) { throw new AnswerValidationException("Invalid choice."); }
            // Must be one of THIS question's options (stops tampered requests).
            if (!rules.allowedOptionIds().contains(id)) throw new AnswerValidationException("Invalid choice.");
            ids.add(id);
        }
        return new ArrayList<>(ids);
    }
}
