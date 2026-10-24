package com.fitness.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class MembershipControllerTests {

	private final MockMvc mockMvc;

	MembershipControllerTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void listsSeededMembershipTypes() throws Exception {
		String token = registerAndLogin("types@example.com");

		mockMvc.perform(get("/api/membership-types").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(3))
			.andExpect(jsonPath("$[0].name").value("Monthly"))
			.andExpect(jsonPath("$[0].durationDays").value(30));
	}

	@Test
	void membershipTypesRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/membership-types")).andExpect(status().isUnauthorized());
	}

	@Test
	void returnsNotFoundWithoutMembership() throws Exception {
		String token = registerAndLogin("nomembership@example.com");

		mockMvc.perform(get("/api/members/me/membership").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("No active membership"));
	}

	@Test
	void buysMembership() throws Exception {
		String token = registerAndLogin("buyer@example.com");
		Long monthlyId = typeId(token, "Monthly");

		LocalDate today = LocalDate.now();
		LocalDate expectedEnd = today.plusDays(29);

		buy(token, monthlyId)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.typeName").value("Monthly"))
			.andExpect(jsonPath("$.startDate").value(today.toString()))
			.andExpect(jsonPath("$.endDate").value(expectedEnd.toString()));

		mockMvc.perform(get("/api/members/me/membership").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.endDate").value(expectedEnd.toString()));
	}

	@Test
	void extendsActiveMembershipFromItsEndDate() throws Exception {
		String token = registerAndLogin("extender@example.com");
		Long monthlyId = typeId(token, "Monthly");
		Long quarterlyId = typeId(token, "Quarterly");

		LocalDate today = LocalDate.now();
		LocalDate expectedEnd = today.plusDays(29 + 90);

		buy(token, monthlyId).andExpect(status().isCreated());

		buy(token, quarterlyId)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.typeName").value("Quarterly"))
			.andExpect(jsonPath("$.startDate").value(today.toString()))
			.andExpect(jsonPath("$.endDate").value(expectedEnd.toString()));
	}

	@Test
	void rejectsUnknownMembershipType() throws Exception {
		String token = registerAndLogin("unknowntype@example.com");

		buy(token, 999999L)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Membership type not found"));
	}

	@Test
	void rejectsMissingMembershipType() throws Exception {
		String token = registerAndLogin("missingtype@example.com");

		mockMvc.perform(post("/api/members/me/memberships")
				.header(HttpHeaders.AUTHORIZATION, bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.typeId").exists());
	}

	@Test
	void returnsQrToken() throws Exception {
		String token = registerAndLogin("qr@example.com");

		mockMvc.perform(get("/api/members/me/qr").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void adminCannotUseMemberEndpoints() throws Exception {
		String token = login("admin@fitness.local", "Admin123!");

		mockMvc.perform(get("/api/members/me/qr").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isForbidden());
	}

	private ResultActions buy(String token, Long typeId) throws Exception {
		String body = """
				{"typeId": %d}
				""".formatted(typeId);

		return mockMvc.perform(post("/api/members/me/memberships")
			.header(HttpHeaders.AUTHORIZATION, bearer(token))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private Long typeId(String token, String name) throws Exception {
		String body = mockMvc.perform(get("/api/membership-types").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<Integer> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
		Integer id = ids.get(0);

		return id.longValue();
	}

	private String registerAndLogin(String email) throws Exception {
		String body = """
				{"email": "%s", "password": "password123", "firstName": "Anna", "lastName": "Nowak"}
				""".formatted(email);

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated());

		return login(email, "password123");
	}

	private String login(String email, String password) throws Exception {
		String credentials = """
				{"email": "%s", "password": "%s"}
				""".formatted(email, password);

		String body = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(credentials))
			.andReturn()
			.getResponse()
			.getContentAsString();

		return JsonPath.read(body, "$.token");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

}
