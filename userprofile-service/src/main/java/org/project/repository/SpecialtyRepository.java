package org.project.repository;


import org.project.model.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    Optional<Specialty> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    List<Specialty> findByIsActiveTrue();

    @Query("SELECT s FROM Specialty s WHERE " +
            "(COALESCE(:name, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:isActive IS NULL OR s.isActive = :isActive)")
    Page<Specialty> findSpecialtiesWithFilters(
            @Param("name") String name,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
