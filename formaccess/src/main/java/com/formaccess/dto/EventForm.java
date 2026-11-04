package com.formaccess.dto;

import com.formaccess.entity.enums.DeadlineMode;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Event details typed by the creator. Date format matches <input type="datetime-local">. */
public class EventForm {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "Start date/time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @NotNull(message = "Deadline is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadlineAt;

    @NotNull
    private DeadlineMode deadlineMode = DeadlineMode.AUTOMATIC;

    private boolean allowMultipleSubmissions;

    private boolean autoReminderEnabled;

    @Min(value = 1, message = "At least 1 hour") @Max(value = 720, message = "At most 720 hours")
    private Integer reminderHoursBefore = 24;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public DeadlineMode getDeadlineMode() { return deadlineMode; }
    public void setDeadlineMode(DeadlineMode deadlineMode) { this.deadlineMode = deadlineMode; }
    public boolean isAllowMultipleSubmissions() { return allowMultipleSubmissions; }
    public void setAllowMultipleSubmissions(boolean v) { this.allowMultipleSubmissions = v; }
    public boolean isAutoReminderEnabled() { return autoReminderEnabled; }
    public void setAutoReminderEnabled(boolean v) { this.autoReminderEnabled = v; }
    public Integer getReminderHoursBefore() { return reminderHoursBefore; }
    public void setReminderHoursBefore(Integer v) { this.reminderHoursBefore = v; }
}
