package com.fitness.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.api.model.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

	Optional<Membership> findFirstByMemberIdOrderByEndDateDesc(UUID memberId);

}
