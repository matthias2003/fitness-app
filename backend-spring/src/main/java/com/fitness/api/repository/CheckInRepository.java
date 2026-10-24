package com.fitness.api.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.api.model.CheckIn;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

	Page<CheckIn> findByMemberId(UUID memberId, Pageable pageable);

}
