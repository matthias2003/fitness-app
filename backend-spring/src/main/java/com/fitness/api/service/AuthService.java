package com.fitness.api.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.dto.LoginRequest;
import com.fitness.api.dto.LoginResponse;
import com.fitness.api.dto.RegisterRequest;
import com.fitness.api.dto.UserResponse;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.UserRepository;

@Service
public class AuthService {

	private static final int BCRYPT_MAX_BYTES = 72;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final TokenService tokenService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, TokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {
		requireBcryptLength(request.password());

		String email = normalizeEmail(request.email());
		String passwordHash = passwordEncoder.encode(request.password());
		String firstName = request.firstName().trim();
		String lastName = request.lastName().trim();

		User user = new User(email, passwordHash, firstName, lastName, Role.MEMBER);

		try {
			User savedUser = userRepository.saveAndFlush(user);
			return UserResponse.from(savedUser);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
		}
	}

	public LoginResponse login(LoginRequest request) {
		requireBcryptLength(request.password());

		String email = normalizeEmail(request.email());
		UsernamePasswordAuthenticationToken credentials =
				UsernamePasswordAuthenticationToken.unauthenticated(email, request.password());

		authenticationManager.authenticate(credentials);

		User user = userRepository.findByEmail(email).orElseThrow();
		String token = tokenService.createToken(user);

		return new LoginResponse(token, UserResponse.from(user));
	}

	public UserResponse getCurrentUser(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));

		return UserResponse.from(user);
	}

	private static void requireBcryptLength(String password) {
		int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;

		if (passwordBytes > BCRYPT_MAX_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
		}
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
