package com.formaccess.entity;

import jakarta.persistence.*;

/** Maps to table "question_options": one choice of a dropdown/radio/checkbox question. */
@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_text", nullable = false, length = 200)
    private String optionText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public QuestionOption() { }

    public QuestionOption(Question question, String optionText, int displayOrder) {
        this.question = question;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
