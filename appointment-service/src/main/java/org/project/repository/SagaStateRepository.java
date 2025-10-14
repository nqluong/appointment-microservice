package org.project.repository;

import org.project.model.Appointment;
import org.project.model.AppointmentSagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SagaStateRepository extends JpaRepository<AppointmentSagaState, String> {
}
