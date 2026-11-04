package com.formaccess.repository;

import com.formaccess.entity.Event;
import com.formaccess.entity.Reminder;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /** Was this user reminded about this event after the given time? (anti-spam) */
    boolean existsByEventAndUserAndSentAtAfter(Event event, User user, LocalDateTime after);

    boolean existsByEventAndUserAndReminderType(Event event, User user, ReminderType type);

    List<Reminder> findByEventOrderBySentAtDesc(Event event);
}
