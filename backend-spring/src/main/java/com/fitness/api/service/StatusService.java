package com.fitness.api.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.fitness.api.client.TrainingApiClient;
import com.fitness.api.dto.ServiceStatus;
import com.fitness.api.dto.SystemStatus;

@Service
public class StatusService {

	private final DataSource dataSource;

	private final TrainingApiClient trainingApiClient;

	public StatusService(DataSource dataSource, TrainingApiClient trainingApiClient) {
		this.dataSource = dataSource;
		this.trainingApiClient = trainingApiClient;
	}

	public SystemStatus getStatus() {
		ServiceStatus core = new ServiceStatus("core-api", "UP", isDatabaseUp() ? "UP" : "DOWN");
		return new SystemStatus(core, trainingApiClient.getStatus());
	}

	private boolean isDatabaseUp() {
		try (Connection connection = dataSource.getConnection()) {
			return connection.isValid(2);
		}
		catch (SQLException ex) {
			return false;
		}
	}

}
