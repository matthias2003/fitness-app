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

import com.fitness.api.model.Membership;
import com.fitness.api.model.MembershipType;
import com.fitness.api.model.User;
import com.fitness.api.repository.MembershipRepository;
import com.fitness.api.repository.MembershipTypeRepository;
import com.fitness.api.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CheckInControllerTests {

	private final MockMvc mockMvc;

	private final UserRepository userRepository;

	private final MembershipTypeRepository membershipTypeRepository;

	private final MembershipRepository membershipRepository;

	CheckInControllerTests(MockMvc mockMvc, UserRepository userRepository,
			MembershipTypeRepository membershipTypeRepository, MembershipRepository membershipRepository) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.membershipTypeRepository = membershipTypeRepository;
		this.membershipRepository = membershipRepository;
	}

	@Test
	void checksInMemberWithValidMembership() throws Exception {
		String memberToken = registerAndLogin("checkin@example.com");
		buyFirstType(memberToken);
		String qrToken = qrToken(memberToken);

		checkIn(qrToken)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.firstName").value("Anna"))
			.andExpect(jsonPath("$.membershipEndDate").exists())
			.andExpect(jsonPath("$.checkedInAt").exists());

		mockMvc.perform(get("/api/members/me/check-ins").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total").value(1));
	}

	@Test
	void deniesEntryWithoutMembership() throws Exception {
		String memberToken = registerAndLogin("nopass@example.com");
		String qrToken = qrToken(memberToken);

		checkIn(qrToken)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Member has no membership"));
	}

	@Test
	void deniesEntryWithExpiredMembership() throws Exception {
		String memberToken = registerAndLogin("expired@example.com");
		String qrToken = qrToken(memberToken);

		User member = userRepository.findByEmail("expired@example.com").orElseThrow();
		MembershipType type = membershipTypeRepository.findAll().get(0);
		LocalDate endDate = LocalDate.now().minusDays(1);
		LocalDate startDate = endDate.minusDays(29);
		Membership expiredMembership = new Membership(member, type, startDate, endDate);
		membershipRepository.save(expiredMembership);

		checkIn(qrToken)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value("Membership expired on " + endDate));
	}

	@Test
	void rejectsUnknownToken() throws Exception {
		checkIn("not-a-real-token")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Unknown QR code"));
	}

	@Test
	void rejectsStaffToken() throws Exception {
		User admin = userRepository.findByEmail("admin@fitness.local").orElseThrow();

		checkIn(admin.getQrToken())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Unknown QR code"));
	}

	@Test
	void trainerChecksInMember() throws Exception {
		String memberToken = registerAndLogin("trainerscan@example.com");
		buyFirstType(memberToken);
		String qrToken = qrToken(memberToken);
		String trainerToken = login("trainer@fitness.local", "Admin123!");

		checkIn(trainerToken, qrToken)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.firstName").value("Anna"));
	}

	@Test
	void memberGetsForbiddenOnStaffEndpoints() throws Exception {
		String memberToken = registerAndLogin("notadmin@example.com");
		String qrToken = qrToken(memberToken);

		checkIn(memberToken, qrToken)
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/members").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isForbidden());
	}

	@Test
	void pagesCheckInHistory() throws Exception {
		String memberToken = registerAndLogin("history@example.com");
		buyFirstType(memberToken);
		String qrToken = qrToken(memberToken);

		checkIn(qrToken).andExpect(status().isCreated());
		checkIn(qrToken).andExpect(status().isCreated());
		checkIn(qrToken).andExpect(status().isCreated());

		mockMvc.perform(get("/api/members/me/check-ins?page=1&size=2")
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(1))
			.andExpect(jsonPath("$.items[0].checkedInAt").exists())
			.andExpect(jsonPath("$.page").value(1))
			.andExpect(jsonPath("$.size").value(2))
			.andExpect(jsonPath("$.total").value(3));
	}

	@Test
	void pagesAdminMembersList() throws Exception {
		registerAndLogin("listed1@example.com");
		registerAndLogin("listed2@example.com");
		String adminToken = adminToken();

		mockMvc.perform(get("/api/admin/members?page=0&size=1").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(1))
			.andExpect(jsonPath("$.items[0].email").exists())
			.andExpect(jsonPath("$.items[0].membershipActive").isBoolean())
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(1))
			.andExpect(jsonPath("$.total").isNumber());
	}

	@Test
	void rejectsInvalidPaging() throws Exception {
		String adminToken = adminToken();

		mockMvc.perform(get("/api/admin/members?size=0").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invalid request parameters"));

		mockMvc.perform(get("/api/admin/members?page=2147483647&size=100")
				.header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invalid request parameters"));
	}

	private ResultActions checkIn(String qrToken) throws Exception {
		String adminToken = adminToken();

		return checkIn(adminToken, qrToken);
	}

	private ResultActions checkIn(String staffToken, String qrToken) throws Exception {
		String body = """
				{"token": "%s"}
				""".formatted(qrToken);

		return mockMvc.perform(post("/api/check-ins")
			.header(HttpHeaders.AUTHORIZATION, bearer(staffToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private void buyFirstType(String memberToken) throws Exception {
		String typesBody = mockMvc.perform(get("/api/membership-types")
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<Integer> ids = JsonPath.read(typesBody, "$[*].id");
		String body = """
				{"typeId": %d}
				""".formatted(ids.get(0));

		mockMvc.perform(post("/api/members/me/memberships")
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated());
	}

	private String qrToken(String memberToken) throws Exception {
		String body = mockMvc.perform(get("/api/members/me/qr").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andReturn()
			.getResponse()
			.getContentAsString();

		return JsonPath.read(body, "$.token");
	}

	private String adminToken() throws Exception {
		return login("admin@fitness.local", "Admin123!");
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
