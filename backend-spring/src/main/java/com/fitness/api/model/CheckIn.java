package com.fitness.api.model;

import java.time.Instant;

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
@Table(name = "check_ins")
public class CheckIn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id")
	private User member;

	@Column(nullable = false)
	private Instant checkedInAt;

	protected CheckIn() {
	}

	public CheckIn(User member) {
		this.member = member;
		this.checkedInAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getMember() {
		return member;
	}

	public Instant getCheckedInAt() {
		return checkedInAt;
	}

}
