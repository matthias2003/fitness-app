package com.fitness.api.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_types")
public class MembershipType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private int durationDays;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	protected MembershipType() {
	}

	public MembershipType(String name, int durationDays, BigDecimal price) {
		this.name = name;
		this.durationDays = durationDays;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public BigDecimal getPrice() {
		return price;
	}

}
