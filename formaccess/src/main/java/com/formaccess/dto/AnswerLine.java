package com.formaccess.dto;

/** One row when showing a submission: the question and the answer as text. */
public class AnswerLine {
    private final String question;
    private final String answer;

    public AnswerLine(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}
