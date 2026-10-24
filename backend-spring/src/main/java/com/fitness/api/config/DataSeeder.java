package com.fitness.api.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fitness.api.model.MembershipType;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.MembershipTypeRepository;
import com.fitness.api.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;

	private final MembershipTypeRepository membershipTypeRepository;

	private final PasswordEncoder passwordEncoder;

	private final String password;

	public DataSeeder(UserRepository userRepository, MembershipTypeRepository membershipTypeRepository,
			PasswordEncoder passwordEncoder, @Value("${seed.password}") String password) {
		this.userRepository = userRepository;
		this.membershipTypeRepository = membershipTypeRepository;
		this.passwordEncoder = passwordEncoder;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		createIfMissing("admin@fitness.local", "System", "Admin", Role.ADMIN);
		createIfMissing("trainer@fitness.local", "John", "Trainer", Role.TRAINER);

		createTypeIfMissing("Monthly", 30, new BigDecimal("129.00"));
		createTypeIfMissing("Quarterly", 90, new BigDecimal("349.00"));
		createTypeIfMissing("Annual", 365, new BigDecimal("1199.00"));
	}

	private void createIfMissing(String email, String firstName, String lastName, Role role) {
		if (!userRepository.existsByEmail(email)) {
			userRepository.save(new User(email, passwordEncoder.encode(password), firstName, lastName, role));
		}
	}

	private void createTypeIfMissing(String name, int durationDays, BigDecimal price) {
		if (!membershipTypeRepository.existsByName(name)) {
			MembershipType type = new MembershipType(name, durationDays, price);
			membershipTypeRepository.save(type);
		}
	}

}
