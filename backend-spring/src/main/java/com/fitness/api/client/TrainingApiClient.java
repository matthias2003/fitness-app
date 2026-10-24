package com.fitness.api.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fitness.api.dto.ServiceStatus;

@Component
public class TrainingApiClient {

	private static final String SERVICE_NAME = "training-api";

	private final RestClient restClient;

	public TrainingApiClient(@Value("${training-api.base-url}") String baseUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(5));
		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}

	public ServiceStatus getStatus() {
		try {
			ServiceStatus status = restClient.get().uri("/status").retrieve().body(ServiceStatus.class);
			return status != null ? status : unavailable();
		}
		catch (RestClientException ex) {
			return unavailable();
		}
	}

	private static ServiceStatus unavailable() {
		return new ServiceStatus(SERVICE_NAME, "DOWN", "UNKNOWN");
	}

}
