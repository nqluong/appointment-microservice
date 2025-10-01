package org.project.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.repository.SlotGenerationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationRepositoryImpl implements SlotGenerationRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String CALL_PROCEDURE = "CALL generate_slots_for_range(?, ?, ?)";
    private static final String COUNT_SLOTS_QUERY =
            "SELECT COUNT(*) FROM doctor_available_slots " +
                    "WHERE doctor_user_id = ? AND slot_date BETWEEN ? AND ? AND is_available = true";

    @Override
    public void generateSlotsForRange(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        try {
            jdbcTemplate.update(CALL_PROCEDURE, doctorId, startDate, endDate);
        } catch (Exception e) {
            log.error("Error generating slots for doctor: {}", doctorId, e);
            throw new RuntimeException("Failed to generate slots", e);
        }
    }

    @Override
    public long countAvailableSlots(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        try {
            Long count = jdbcTemplate.queryForObject(COUNT_SLOTS_QUERY, Long.class,
                    doctorId, startDate, endDate);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error counting slots for doctor: {}", doctorId, e);
            return 0;
        }
    }
}
