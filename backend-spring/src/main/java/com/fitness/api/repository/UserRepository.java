package com.fitness.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.api.model.Role;
import com.fitness.api.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	Optional<User> findByQrTokenAndRole(String qrToken, Role role);

	boolean existsByEmail(String email);

	Page<User> findByRole(Role role, Pageable pageable);

}
