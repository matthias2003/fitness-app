package com.fitness.api.controller;

import java.time.Duration;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.api.dto.AuthSession;
import com.fitness.api.dto.LoginRequest;
import com.fitness.api.dto.LoginResponse;
import com.fitness.api.dto.RegisterRequest;
import com.fitness.api.dto.UserResponse;
import com.fitness.api.model.RefreshToken;
import com.fitness.api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String REFRESH_COOKIE = "refresh_token";

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthSession session = authService.login(request);

		ResponseCookie cookie = refreshCookie(session.refreshToken(), RefreshToken.LIFETIME);
		LoginResponse body = new LoginResponse(session.accessToken(), session.user());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(body);
	}

	@PostMapping("/refresh")
	public LoginResponse refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		return authService.refresh(refreshToken);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		authService.logout(refreshToken);

		ResponseCookie cookie = refreshCookie("", Duration.ZERO);

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		UUID userId = UUID.fromString(jwt.getSubject());

		return authService.getCurrentUser(userId);
	}

	private static ResponseCookie refreshCookie(String value, Duration maxAge) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/auth")
			.maxAge(maxAge)
			.build();
	}

}
