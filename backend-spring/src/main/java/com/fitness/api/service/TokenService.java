package com.fitness.api.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.fitness.api.model.User;

@Service
public class TokenService {

	private static final Duration TOKEN_LIFETIME = Duration.ofHours(8);

	private final JwtEncoder jwtEncoder;

	public TokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String createToken(User user) {
		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.subject(user.getId().toString())
			.claim("role", user.getRole().name())
			.issuedAt(now)
			.expiresAt(now.plus(TOKEN_LIFETIME))
			.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);

		return jwtEncoder.encode(parameters).getTokenValue();
	}

}
