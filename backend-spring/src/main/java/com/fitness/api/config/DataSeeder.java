package com.fitness.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final String password;

	public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${seed.password}") String password) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		createIfMissing("admin@fitness.local", "System", "Admin", Role.ADMIN);
		createIfMissing("trainer@fitness.local", "John", "Trainer", Role.TRAINER);
	}

	private void createIfMissing(String email, String firstName, String lastName, Role role) {
		if (!userRepository.existsByEmail(email)) {
			userRepository.save(new User(email, passwordEncoder.encode(password), firstName, lastName, role));
		}
	}

}
