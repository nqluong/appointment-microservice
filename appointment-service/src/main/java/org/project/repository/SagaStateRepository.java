package org.project.repository;

import java.util.Optional;
import java.util.UUID;

import org.project.model.AppointmentSagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaStateRepository extends JpaRepository<AppointmentSagaState, String> {
    Optional<AppointmentSagaState> findByAppointmentId(UUID appointmentId);
}
