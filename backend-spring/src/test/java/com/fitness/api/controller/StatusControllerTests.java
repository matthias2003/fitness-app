package com.fitness.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.ServiceStatus;

@SpringBootTest
@AutoConfigureMockMvc
class StatusControllerTests {

	private final MockMvc mockMvc;

	@MockitoBean
	private TrainingApiClient trainingApiClient;

	StatusControllerTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	private static RequestPostProcessor asRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Test
	void returnsStatusOfBothApis() throws Exception {
		given(trainingApiClient.getStatus()).willReturn(new ServiceStatus("training-api", "UP", "UP"));

		mockMvc.perform(get("/api/status").with(asRole("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.core.service").value("core-api"))
			.andExpect(jsonPath("$.core.database").value("UP"))
			.andExpect(jsonPath("$.training.service").value("training-api"))
			.andExpect(jsonPath("$.training.status").value("UP"));
	}

	@Test
	void reportsTrainingApiAsDownWhenUnreachable() throws Exception {
		given(trainingApiClient.getStatus()).willReturn(new ServiceStatus("training-api", "DOWN", "UNKNOWN"));

		mockMvc.perform(get("/api/status").with(asRole("ADMIN")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.core.status").value("UP"))
			.andExpect(jsonPath("$.training.status").value("DOWN"));
	}

	@Test
	void requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/status")).andExpect(status().isUnauthorized());
	}

	@Test
	void isForbiddenForNonAdmins() throws Exception {
		mockMvc.perform(get("/api/status").with(asRole("MEMBER"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/api/status").with(asRole("TRAINER"))).andExpect(status().isForbidden());
	}

}
