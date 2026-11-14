package com.fitness.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.api.model.PlanAssignment;

public interface PlanAssignmentRepository extends JpaRepository<PlanAssignment, Long> {

	Optional<PlanAssignment> findFirstByMemberIdOrderByAssignedAtDesc(UUID memberId);

	List<PlanAssignment> findByTrainerIdOrderByMemberLastNameAsc(UUID trainerId);

}
