package com.fitness.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.CompleteSummaryResponse;
import com.fitness.api.dto.LogSetRequest;
import com.fitness.api.dto.NewRecordResponse;
import com.fitness.api.dto.PageResponse;
import com.fitness.api.dto.PersonalRecordResponse;
import com.fitness.api.dto.PlanExerciseData;
import com.fitness.api.dto.SessionSummaryResponse;
import com.fitness.api.dto.TrainingDayData;
import com.fitness.api.dto.TrainingPlanData;
import com.fitness.api.dto.WorkoutSessionResponse;
import com.fitness.api.dto.WorkoutSetResponse;
import com.fitness.api.model.CheckIn;
import com.fitness.api.model.Goal;
import com.fitness.api.model.Level;
import com.fitness.api.model.User;
import com.fitness.api.repository.CheckInRepository;
import com.fitness.api.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutControllerTests {

	private final MockMvc mockMvc;

	private final UserRepository userRepository;

	private final CheckInRepository checkInRepository;

	@MockitoBean
	private TrainingApiClient trainingApiClient;

	WorkoutControllerTests(MockMvc mockMvc, UserRepository userRepository, CheckInRepository checkInRepository) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
		this.checkInRepository = checkInRepository;
	}

	@Test
	void checkInStartsSessionAndStoresItsId() throws Exception {
		String memberToken = registerAndLogin("workout-checkin@example.com");
		UUID memberId = userId("workout-checkin@example.com");
		WorkoutSessionResponse session = sampleSession(memberId);
		given(trainingApiClient.startSession(memberId, null)).willReturn(session);
		buyFirstType(memberToken);
		String qrToken = qrToken(memberToken);

		checkIn(qrToken)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.trainingSessionId").value(session.id().toString()));

		PageRequest firstPage = PageRequest.of(0, 1);
		Page<CheckIn> checkIns = checkInRepository.findByMemberId(memberId, firstPage);
		CheckIn checkIn = checkIns.getContent().get(0);
		assertThat(checkIn.getTrainingSessionId()).isEqualTo(session.id());
	}

	@Test
	void checkInSucceedsWithoutSessionWhenTrainingServiceIsDown() throws Exception {
		given(trainingApiClient.startSession(any(), any())).willThrow(unavailable());
		String memberToken = registerAndLogin("workout-checkin-down@example.com");
		buyFirstType(memberToken);
		String qrToken = qrToken(memberToken);

		checkIn(qrToken)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.firstName").value("Anna"))
			.andExpect(jsonPath("$.trainingSessionId").value(nullValue()));
	}

	@Test
	void manualStartPassesLatestPlanId() throws Exception {
		TrainingPlanData plan = samplePlan();
		given(trainingApiClient.createPlan(any(), any(), any(), any())).willReturn(plan);
		String memberToken = registerAndLogin("workout-start@example.com");
		UUID memberId = userId("workout-start@example.com");
		WorkoutSessionResponse session = sampleSession(memberId);
		given(trainingApiClient.startSession(memberId, plan.id())).willReturn(session);
		createPlan(memberId);

		mockMvc.perform(post("/api/workouts/sessions").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(session.id().toString()))
			.andExpect(jsonPath("$.plannedDay.name").value("Full body A"));

		verify(trainingApiClient).startSession(memberId, plan.id());
	}

	@Test
	void returnsNotFoundWithoutActiveOrKnownWorkout() throws Exception {
		String memberToken = registerAndLogin("workout-none@example.com");
		UUID unknownId = UUID.randomUUID();

		mockMvc.perform(get("/api/workouts/sessions/current").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("No active workout"));

		mockMvc.perform(get("/api/workouts/sessions/" + unknownId)
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Workout not found"));
	}

	@Test
	void forwardsLoggedSet() throws Exception {
		String memberToken = registerAndLogin("workout-set@example.com");
		UUID memberId = userId("workout-set@example.com");
		WorkoutSessionResponse session = sampleSession(memberId);
		given(trainingApiClient.logSet(eq(memberId), eq(session.id()), any())).willReturn(session);

		logSet(memberToken, session.id(), "{\"exerciseId\": 1, \"reps\": 5, \"weight\": 62.5}")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sets[0].exerciseName").value("Bench Press"))
			.andExpect(jsonPath("$.sets[0].weight").value(62.5));

		LogSetRequest expectedRequest = new LogSetRequest(1, 5, new BigDecimal("62.5"));
		verify(trainingApiClient).logSet(memberId, session.id(), expectedRequest);
	}

	@Test
	void rejectsInvalidSetBody() throws Exception {
		String memberToken = registerAndLogin("workout-invalid@example.com");
		UUID sessionId = UUID.randomUUID();

		logSet(memberToken, sessionId, "{\"exerciseId\": 1, \"reps\": 0, \"weight\": 50}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.reps").value("Reps must be between 1 and 100"));

		logSet(memberToken, sessionId, "{\"exerciseId\": 1, \"reps\": 5, \"weight\": 501}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.weight").value("Weight must be between 0 and 500 kg"));

		logSet(memberToken, sessionId, "{\"reps\": 5, \"weight\": 50}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.exerciseId").value("Exercise is required"));

		verify(trainingApiClient, never()).logSet(any(), any(), any());
	}

	@Test
	void rejectsFractionalReps() throws Exception {
		String memberToken = registerAndLogin("workout-fraction@example.com");
		UUID memberId = userId("workout-fraction@example.com");
		WorkoutSessionResponse session = sampleSession(memberId);
		given(trainingApiClient.logSet(eq(memberId), eq(session.id()), any())).willReturn(session);

		logSet(memberToken, session.id(), "{\"exerciseId\": 1, \"reps\": 1.5, \"weight\": 50}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Malformed request"));

		logSet(memberToken, session.id(), "{\"exerciseId\": 1, \"reps\": 2, \"weight\": 50}")
			.andExpect(status().isCreated());
	}

	@Test
	void passesConflictThrough() throws Exception {
		ResponseStatusException conflict = new ResponseStatusException(HttpStatus.CONFLICT,
				"Workout already completed");
		given(trainingApiClient.logSet(any(), any(), any())).willThrow(conflict);
		String memberToken = registerAndLogin("workout-conflict@example.com");

		logSet(memberToken, UUID.randomUUID(), "{\"exerciseId\": 1, \"reps\": 5, \"weight\": 50}")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("Workout already completed"));
	}

	@Test
	void completesWorkout() throws Exception {
		String memberToken = registerAndLogin("workout-complete@example.com");
		UUID memberId = userId("workout-complete@example.com");
		UUID sessionId = UUID.randomUUID();
		NewRecordResponse newRecord = new NewRecordResponse(1, "Bench Press", new BigDecimal("70"),
				new BigDecimal("65"));
		CompleteSummaryResponse summary = new CompleteSummaryResponse(sessionId, Instant.now(), 3,
				new BigDecimal("1050"), new BigDecimal("900"), List.of(newRecord));
		given(trainingApiClient.completeSession(memberId, sessionId)).willReturn(summary);

		mockMvc.perform(post("/api/workouts/sessions/" + sessionId + "/complete")
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.setsCount").value(3))
			.andExpect(jsonPath("$.previousVolume").value(900))
			.andExpect(jsonPath("$.newRecords[0].maxWeight").value(70))
			.andExpect(jsonPath("$.newRecords[0].previousMaxWeight").value(65));
	}

	@Test
	void pagesWorkoutHistory() throws Exception {
		String memberToken = registerAndLogin("workout-history@example.com");
		UUID memberId = userId("workout-history@example.com");
		SessionSummaryResponse item = new SessionSummaryResponse(UUID.randomUUID(), Instant.now(), Instant.now(),
				"Full body A", 4, new BigDecimal("1200"));
		PageResponse<SessionSummaryResponse> page = new PageResponse<>(List.of(item), 1, 5, 6);
		given(trainingApiClient.getSessions(memberId, 1, 5)).willReturn(page);

		mockMvc.perform(get("/api/workouts/sessions?page=1&size=5")
				.header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].dayName").value("Full body A"))
			.andExpect(jsonPath("$.items[0].setsCount").value(4))
			.andExpect(jsonPath("$.page").value(1))
			.andExpect(jsonPath("$.size").value(5))
			.andExpect(jsonPath("$.total").value(6));

		mockMvc.perform(get("/api/workouts/sessions?size=0").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isBadRequest());

		verify(trainingApiClient, never()).getSessions(any(), anyInt(), eq(0));
	}

	@Test
	void listsRecords() throws Exception {
		String memberToken = registerAndLogin("workout-records@example.com");
		UUID memberId = userId("workout-records@example.com");
		PersonalRecordResponse record = new PersonalRecordResponse(9, "Back Squat", "Legs", new BigDecimal("100"),
				Instant.now());
		given(trainingApiClient.getRecords(memberId)).willReturn(List.of(record));

		mockMvc.perform(get("/api/members/me/records").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].exerciseName").value("Back Squat"))
			.andExpect(jsonPath("$[0].maxWeight").value(100));
	}

	@Test
	void workoutsAreForMembersOnly() throws Exception {
		String trainerToken = login("trainer@fitness.local", "Admin123!");

		mockMvc.perform(get("/api/workouts/sessions/current").header(HttpHeaders.AUTHORIZATION, bearer(trainerToken)))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/workouts/sessions").header(HttpHeaders.AUTHORIZATION, bearer(trainerToken)))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/members/me/records").header(HttpHeaders.AUTHORIZATION, bearer(trainerToken)))
			.andExpect(status().isForbidden());
	}

	@Test
	void returnsServiceUnavailableWhenTrainingServiceIsDown() throws Exception {
		given(trainingApiClient.getCurrentSession(any())).willThrow(unavailable());
		String memberToken = registerAndLogin("workout-down@example.com");

		mockMvc.perform(get("/api/workouts/sessions/current").header(HttpHeaders.AUTHORIZATION, bearer(memberToken)))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.message").value("Training service is unavailable"));
	}

	private static ResponseStatusException unavailable() {
		return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Training service is unavailable");
	}

	private static WorkoutSessionResponse sampleSession(UUID memberId) {
		PlanExerciseData squat = new PlanExerciseData(9, "Back Squat", "Legs", 5, 5);
		TrainingDayData day = new TrainingDayData(1, "Full body A", List.of(squat));
		WorkoutSetResponse set = new WorkoutSetResponse(1L, 1, "Bench Press", 5, new BigDecimal("62.5"),
				Instant.now());

		return new WorkoutSessionResponse(UUID.randomUUID(), memberId, Instant.now(), null, day, List.of(set));
	}

	private static TrainingPlanData samplePlan() {
		PlanExerciseData squat = new PlanExerciseData(9, "Back Squat", "Legs", 5, 5);
		TrainingDayData day = new TrainingDayData(1, "Full body A", List.of(squat));

		return new TrainingPlanData(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Goal.STRENGTH,
				Level.BEGINNER, "Full body", Instant.now(), List.of(day));
	}

	private void createPlan(UUID memberId) throws Exception {
		String trainerToken = login("trainer@fitness.local", "Admin123!");

		mockMvc.perform(post("/api/members/" + memberId + "/training-plan")
				.header(HttpHeaders.AUTHORIZATION, bearer(trainerToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"goal\": \"STRENGTH\", \"level\": \"BEGINNER\"}"))
			.andExpect(status().isCreated());
	}

	private ResultActions logSet(String memberToken, UUID sessionId, String body) throws Exception {
		return mockMvc.perform(post("/api/workouts/sessions/" + sessionId + "/sets")
			.header(HttpHeaders.AUTHORIZATION, bearer(memberToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private ResultActions checkIn(String qrToken) throws Exception {
		String adminToken = login("admin@fitness.local", "Admin123!");
		String body = """
				{"token": "%s"}
				""".formatted(qrToken);

		return mockMvc.perform(post("/api/check-ins")
			.header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
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

	private UUID userId(String email) {
		User user = userRepository.findByEmail(email).orElseThrow();

		return user.getId();
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
