package com.formaccess.entity;

import com.formaccess.entity.enums.DeadlineMode;
import com.formaccess.entity.enums.EventStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Maps to table "events". An event is one form with its questions and rules. */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    /** Random UUID used in the share link /e/{publicToken}. */
    @Column(name = "public_token", nullable = false, length = 36)
    private String publicToken;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_mode", nullable = false, length = 20)
    private DeadlineMode deadlineMode = DeadlineMode.AUTOMATIC;

    @Column(name = "allow_multiple_submissions", nullable = false)
    private boolean allowMultipleSubmissions;

    @Column(name = "auto_reminder_enabled", nullable = false)
    private boolean autoReminderEnabled;

    @Column(name = "reminder_hours_before")
    private Integer reminderHoursBefore;

    /** One event -> many questions. Saving/deleting the event saves/deletes them too. */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("groupNo ASC, id ASC")
    private List<EligibilityRule> rules = new ArrayList<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    /** True when the automatic deadline has passed (time check, independent of the scheduler). */
    public boolean isDeadlinePassed(LocalDateTime now) {
        return deadlineMode == DeadlineMode.AUTOMATIC && !now.isBefore(deadlineAt);
    }

    public boolean isEditable() { return status == EventStatus.DRAFT; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPublicToken() { return publicToken; }
    public void setPublicToken(String publicToken) { this.publicToken = publicToken; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public DeadlineMode getDeadlineMode() { return deadlineMode; }
    public void setDeadlineMode(DeadlineMode deadlineMode) { this.deadlineMode = deadlineMode; }
    public boolean isAllowMultipleSubmissions() { return allowMultipleSubmissions; }
    public void setAllowMultipleSubmissions(boolean v) { this.allowMultipleSubmissions = v; }
    public boolean isAutoReminderEnabled() { return autoReminderEnabled; }
    public void setAutoReminderEnabled(boolean v) { this.autoReminderEnabled = v; }
    public Integer getReminderHoursBefore() { return reminderHoursBefore; }
    public void setReminderHoursBefore(Integer v) { this.reminderHoursBefore = v; }
    public List<Question> getQuestions() { return questions; }
    public List<EligibilityRule> getRules() { return rules; }
}
