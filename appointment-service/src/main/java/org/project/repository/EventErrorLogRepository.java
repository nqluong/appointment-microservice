package org.project.repository;

import org.project.model.EventErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventErrorLogRepository extends JpaRepository<EventErrorLog, UUID> {
}
