package com.fitness.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.ExerciseResponse;
import com.fitness.api.dto.PlanExerciseData;
import com.fitness.api.dto.TrainingDayData;
import com.fitness.api.dto.TrainingPlanData;
import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;
import com.fitness.api.model.PlanAssignment;
import com.fitness.api.model.Role;
import com.fitness.api.model.User;
import com.fitness.api.repository.PlanAssignmentRepository;
import com.fitness.api.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingPlanControllerTests {

	private final MockMvc mockMvc;

	private final UserRepository userRepository;

	private final PlanAssignmentRepository planAssignmentRepository;

	private final PasswordEncoder passwordEncoder;

	@MockitoBean
	private TrainingApiClient trainingApiClient;

	TrainingPlanControllerTests(MockMvc mockMvc, UserRepository userRepository,
			PlanAssignmentRepository planAssignmentRepository, PasswordEncoder passwordEncoder) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.planAssignmentRepository = planAssignmentRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Test
	void forwardsExercises() throws Exception {
		ExerciseResponse squat = new ExerciseResponse(9, "Back Squat", "Legs", "Barbell");
		given(trainingApiClient.getExercises()).willReturn(List.of(squat));
		String memberToken = registerAndLogin("plan-exercises@example.com");

		mockMvc.perform(get("/api/exercises").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("Back Squat"));
	}

	@Test
	void trainerCreatesPlanAndMemberReadsIt() throws Exception {
		mockTrainingService();
		String memberToken = registerAndLogin("plan-create@example.com");
		UUID memberId = userId("plan-create@example.com");

		createPlan(trainerToken(), memberId, "STRENGTH")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.memberName").value("Anna Nowak"))
			.andExpect(jsonPath("$.authorName").value("John Trainer"));

		mockMvc.perform(get("/api/members/me/training-plan").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authorName").value("John Trainer"))
			.andExpect(jsonPath("$.days[0].name").value("Full body A"));

		mockMvc.perform(get("/api/members/" + memberId + "/training-plan")
				.header(HttpHeaders.AUTHORIZATION, bearer(trainerToken())))
			.andExpect(status().isOk());
	}

	@Test
	void returnsNotFoundWithoutPlan() throws Exception {
		String memberToken = registerAndLogin("plan-none@example.com");

		mockMvc.perform(get("/api/members/me/training-plan").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("No training plan yet"));
	}

	@Test
	void memberCannotCreatePlan() throws Exception {
		String memberToken = registerAndLogin("plan-forbidden@example.com");
		UUID memberId = userId("plan-forbidden@example.com");

		createPlan(memberToken, memberId, "STRENGTH").andExpect(status().isForbidden());
	}

	@Test
	void returnsServiceUnavailableAndSavesNothingWhenTrainingServiceIsDown() throws Exception {
		ResponseStatusException unavailable = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
				"Training service is unavailable");
		given(trainingApiClient.createPlan(any(), any(), any(), any())).willThrow(unavailable);
		registerAndLogin("plan-down@example.com");
		UUID memberId = userId("plan-down@example.com");

		createPlan(trainerToken(), memberId, "STRENGTH").andExpect(status().isServiceUnavailable());

		Optional<PlanAssignment> assignment = planAssignmentRepository
			.findFirstByMemberIdOrderByAssignedAtDesc(memberId);
		assertThat(assignment).isEmpty();
	}

	@Test
	void rejectsUnknownMemberAndInvalidGoal() throws Exception {
		registerAndLogin("plan-invalid@example.com");
		UUID memberId = userId("plan-invalid@example.com");
		UUID unknownId = UUID.randomUUID();

		createPlan(trainerToken(), unknownId, "STRENGTH")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Member not found"));

		createPlan(trainerToken(), memberId, "FLYING").andExpect(status().isBadRequest());
	}

	@Test
	void listsTrainerClients() throws Exception {
		mockTrainingService();
		String trainerToken = createTrainerAndLogin("plan-coach@example.com");
		registerAndLogin("plan-client-b@example.com", "Zoe", "Baker");
		registerAndLogin("plan-client-a@example.com", "Adam", "Abbott");
		UUID firstClientId = userId("plan-client-b@example.com");
		UUID secondClientId = userId("plan-client-a@example.com");

		createPlan(trainerToken, firstClientId, "STRENGTH").andExpect(status().isCreated());
		createPlan(trainerToken, firstClientId, "MUSCLE").andExpect(status().isCreated());
		createPlan(trainerToken, secondClientId, "ENDURANCE").andExpect(status().isCreated());

		mockMvc.perform(get("/api/trainers/me/clients").header(HttpHeaders.AUTHORIZATION, bearer(trainerToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].lastName").value("Abbott"))
			.andExpect(jsonPath("$[1].lastName").value("Baker"));
	}

	@Test
	void membersListIsForStaffOnly() throws Exception {
		String memberToken = registerAndLogin("plan-list@example.com");

		mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(trainerToken())))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isForbidden());
	}

	private void mockTrainingService() {
		TrainingPlanData plan = samplePlan();
		Optional<TrainingPlanData> foundPlan = Optional.of(plan);

		given(trainingApiClient.createPlan(any(), any(), any(), any())).willReturn(plan);
		given(trainingApiClient.getPlan(any())).willReturn(foundPlan);
	}

	private static TrainingPlanData samplePlan() {
		PlanExerciseData squat = new PlanExerciseData(9, "Back Squat", "Legs", 5, 5);
		TrainingDayData day = new TrainingDayData(1, "Full body A", List.of(squat));

		return new TrainingPlanData(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Goal.STRENGTH,
				Level.BEGINNER, "Full body", Instant.now(), List.of(day));
	}

	private ResultActions createPlan(String token, UUID memberId, String goal) throws Exception {
		String body = """
				{"goal": "%s", "level": "BEGINNER"}
				""".formatted(goal);

		return mockMvc.perform(post("/api/members/" + memberId + "/training-plan")
			.header(HttpHeaders.AUTHORIZATION, bearer(token))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private UUID userId(String email) {
		User user = userRepository.findByEmail(email).orElseThrow();

		return user.getId();
	}

	private String trainerToken() throws Exception {
		return login("trainer@fitness.local", "Admin123!");
	}

	private String createTrainerAndLogin(String email) throws Exception {
		String passwordHash = passwordEncoder.encode("password123");
		User trainer = new User(email, passwordHash, "Kate", "Coach", Role.TRAINER);
		userRepository.save(trainer);

		return login(email, "password123");
	}

	private String registerAndLogin(String email) throws Exception {
		return registerAndLogin(email, "Anna", "Nowak");
	}

	private String registerAndLogin(String email, String firstName, String lastName) throws Exception {
		String body = """
				{"email": "%s", "password": "password123", "firstName": "%s", "lastName": "%s"}
				""".formatted(email, firstName, lastName);

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
