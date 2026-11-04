package com.formaccess.dto;

import com.formaccess.entity.Question;

import java.util.Set;

/** A question plus what is currently typed/selected and any error, for rendering the form. */
public class QuestionInputView {
    private final Question question;
    private final String value;
    private final Set<Long> selected;
    private final String error;

    public QuestionInputView(Question question, String value, Set<Long> selected, String error) {
        this.question = question;
        this.value = value;
        this.selected = selected;
        this.error = error;
    }

    public Question getQuestion() { return question; }
    public String getValue() { return value; }
    public Set<Long> getSelected() { return selected; }
    public String getError() { return error; }
    public boolean isSelected(Long optionId) { return selected.contains(optionId); }
}
