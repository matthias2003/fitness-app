package com.fitness.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiDocsTests {

	private final MockMvc mockMvc;

	ApiDocsTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void openApiDocsAreOpenAndListEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("Fitness App API"))
			.andExpect(jsonPath("$.paths['/api/auth/login']").exists())
			.andExpect(jsonPath("$.paths['/api/members/{memberId}/training-plan']").exists());
	}

	@Test
	void swaggerUiIsOpen() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
			.andExpect(status().isOk());
	}

}
