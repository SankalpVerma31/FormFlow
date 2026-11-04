package com.formaccess.repository;

import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.entity.enums.DeadlineMode;
import com.formaccess.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByPublicToken(String publicToken);

    List<Event> findByCreatorOrderByCreatedAtDesc(User creator);

    List<Event> findAllByOrderByCreatedAtDesc();

    List<Event> findByStatusInOrderByDeadlineAtAsc(Collection<EventStatus> statuses);

    long countByStatus(EventStatus status);

    /** Used by the scheduler: OPEN + AUTOMATIC events whose deadline has passed. */
    List<Event> findByStatusAndDeadlineModeAndDeadlineAtLessThanEqual(
            EventStatus status, DeadlineMode mode, LocalDateTime time);

    /** Used by the scheduler: SCHEDULED events whose start time has arrived. */
    List<Event> findByStatusAndStartAtLessThanEqual(EventStatus status, LocalDateTime time);

    List<Event> findByStatusAndAutoReminderEnabledTrue(EventStatus status);
}
