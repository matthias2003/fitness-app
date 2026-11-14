package com.fitness.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;
import com.fitness.api.model.PlanAssignment;
import com.fitness.api.model.User;

public record TrainingPlanResponse(UUID id, UUID memberId, UUID authorId, Goal goal, Level level, String template,
		Instant createdAt, List<TrainingDayData> days, String memberName, String authorName, Instant assignedAt) {

	public static TrainingPlanResponse from(TrainingPlanData plan, PlanAssignment assignment) {
		String memberName = fullName(assignment.getMember());
		String authorName = fullName(assignment.getTrainer());

		return new TrainingPlanResponse(plan.id(), plan.memberId(), plan.authorId(), plan.goal(), plan.level(),
				plan.template(), plan.createdAt(), plan.days(), memberName, authorName, assignment.getAssignedAt());
	}

	private static String fullName(User user) {
		return user.getFirstName() + " " + user.getLastName();
	}

}
