package com.formaccess.validation;

import java.math.BigDecimal;
import java.util.List;

/** A cleaned, checked answer ready to be stored. empty() = optional question left blank. */
public record ValidatedAnswer(String text, BigDecimal number, List<Long> optionIds) {

    public static ValidatedAnswer empty() { return new ValidatedAnswer(null, null, List.of()); }

    public boolean isEmpty() { return text == null && number == null && optionIds.isEmpty(); }
}
