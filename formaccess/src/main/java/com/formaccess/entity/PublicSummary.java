package com.formaccess.entity;

import com.formaccess.entity.enums.SummaryType;
import com.formaccess.entity.enums.Visibility;
import jakarta.persistence.*;

/** Maps to table "public_summaries": which statistic is visible, and when. */
@Entity
@Table(name = "public_summaries")
public class PublicSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;              // null for event-level summaries

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", nullable = false, length = 30)
    private SummaryType summaryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 30)
    private Visibility visibility = Visibility.PRIVATE;

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public SummaryType getSummaryType() { return summaryType; }
    public void setSummaryType(SummaryType summaryType) { this.summaryType = summaryType; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}
