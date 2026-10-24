package com.fitness.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthControllerTests {

	private final MockMvc mockMvc;

	AuthControllerTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void registersNewMember() throws Exception {
		register("anna@example.com", "password123")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value("anna@example.com"))
			.andExpect(jsonPath("$.role").value("MEMBER"));
	}

	@Test
	void rejectsDuplicateEmail() throws Exception {
		register("duplicate@example.com", "password123").andExpect(status().isCreated());

		register("Duplicate@Example.com", "password123").andExpect(status().isConflict());
	}

	@Test
	void rejectsInvalidRegistration() throws Exception {
		register("not-an-email", "short")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.email").exists())
			.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	void rejectsTooLongPassword() throws Exception {
		register("longpassword@example.com", "a".repeat(73))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	void rejectsPasswordLongerThan72Bytes() throws Exception {
		register("multibyte@example.com", "ą".repeat(40))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Password is too long"));
	}

	@Test
	void rejectsTooLongEmail() throws Exception {
		String label = "b".repeat(63);
		register("a".repeat(64) + "@" + label + "." + label + "." + label + ".com", "password123")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.email").value("Email is too long"));
	}

	@Test
	void rejectsWrongPassword() throws Exception {
		register("wrong@example.com", "password123").andExpect(status().isCreated());

		login("wrong@example.com", "badpassword").andExpect(status().isUnauthorized());
	}

	@Test
	void loginReturnsTokenAndUser() throws Exception {
		register("token@example.com", "password123").andExpect(status().isCreated());

		login("token@example.com", "password123")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.user.email").value("token@example.com"))
			.andExpect(jsonPath("$.user.role").value("MEMBER"));
	}

	@Test
	void meRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void meRejectsInvalidToken() throws Exception {
		mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meReturnsLoggedInUser() throws Exception {
		String token = loginAs("admin@fitness.local", "Admin123!");

		mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("admin@fitness.local"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void loginRejectsFormSubmission() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.content("email=admin@fitness.local&password=Admin123!"))
			.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	void memberCannotAccessAdminEndpoints() throws Exception {
		register("member@example.com", "password123").andExpect(status().isCreated());
		String token = loginAs("member@example.com", "password123");

		mockMvc.perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, bearer(token)))
			.andExpect(status().isForbidden());
	}

	private ResultActions register(String email, String password) throws Exception {
		String body = """
				{"email": "%s", "password": "%s", "firstName": "Anna", "lastName": "Nowak"}
				""".formatted(email, password);

		return mockMvc.perform(post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private ResultActions login(String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(credentials(email, password)));
	}

	private String loginAs(String email, String password) throws Exception {
		String body = login(email, password).andReturn().getResponse().getContentAsString();

		return JsonPath.read(body, "$.token");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	private static String credentials(String email, String password) {
		return """
				{"email": "%s", "password": "%s"}
				""".formatted(email, password);
	}

}
