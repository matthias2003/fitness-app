package com.fitness.api.model;

import java.time.Duration;
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
@Table(name = "refresh_tokens")
public class RefreshToken {

	public static final Duration LIFETIME = Duration.ofDays(7);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private Instant expiresAt;

	protected RefreshToken() {
	}

	public RefreshToken(User user) {
		this.token = UUID.randomUUID().toString();
		this.user = user;
		this.expiresAt = Instant.now().plus(LIFETIME);
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

}
