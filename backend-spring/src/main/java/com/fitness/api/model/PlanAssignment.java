package com.fitness.api.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_assignments")
public class PlanAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id")
	private User member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trainer_id")
	private User trainer;

	@Column(nullable = false)
	private UUID trainingPlanId;

	@Column(nullable = false)
	private Instant assignedAt;

	protected PlanAssignment() {
	}

	public PlanAssignment(User member, User trainer, UUID trainingPlanId) {
		this.member = member;
		this.trainer = trainer;
		this.trainingPlanId = trainingPlanId;
		this.assignedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getMember() {
		return member;
	}

	public User getTrainer() {
		return trainer;
	}

	public UUID getTrainingPlanId() {
		return trainingPlanId;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

}
