package com.fitness.api.model;

import java.time.LocalDate;

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
@Table(name = "memberships")
public class Membership {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id")
	private User member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "type_id")
	private MembershipType type;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	protected Membership() {
	}

	public Membership(User member, MembershipType type, LocalDate startDate, LocalDate endDate) {
		this.member = member;
		this.type = type;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public boolean isActiveOn(LocalDate date) {
		boolean started = !date.isBefore(startDate);
		boolean notEnded = !date.isAfter(endDate);

		return started && notEnded;
	}

	public void extend(MembershipType newType) {
		this.type = newType;
		this.endDate = endDate.plusDays(newType.getDurationDays());
	}

	public Long getId() {
		return id;
	}

	public User getMember() {
		return member;
	}

	public MembershipType getType() {
		return type;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

}
