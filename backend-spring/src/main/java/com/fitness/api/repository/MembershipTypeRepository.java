package com.fitness.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.api.model.MembershipType;

public interface MembershipTypeRepository extends JpaRepository<MembershipType, Long> {

	boolean existsByName(String name);

}
