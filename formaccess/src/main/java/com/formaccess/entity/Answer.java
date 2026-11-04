package com.formaccess.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Maps to table "answers". Text answers use answerText, numbers use answerNumber,
 * and choice answers list their chosen options in the "answer_selections" table.
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "answer_number", precision = 15, scale = 4)
    private BigDecimal answerNumber;

    /** Many-to-many through the bridge table answer_selections. */
    @ManyToMany
    @JoinTable(name = "answer_selections",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id"))
    private Set<QuestionOption> selectedOptions = new LinkedHashSet<>();

    public Long getId() { return id; }
    public Submission getSubmission() { return submission; }
    public void setSubmission(Submission submission) { this.submission = submission; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public BigDecimal getAnswerNumber() { return answerNumber; }
    public void setAnswerNumber(BigDecimal answerNumber) { this.answerNumber = answerNumber; }
    public Set<QuestionOption> getSelectedOptions() { return selectedOptions; }
}
