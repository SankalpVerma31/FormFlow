package com.formaccess.repository;

import com.formaccess.entity.Event;
import com.formaccess.entity.PublicSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicSummaryRepository extends JpaRepository<PublicSummary, Long> {
    List<PublicSummary> findByEvent(Event event);
}
