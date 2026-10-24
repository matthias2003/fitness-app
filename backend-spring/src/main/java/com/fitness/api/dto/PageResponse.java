package com.fitness.api.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> items, int page, int size, long total) {

	public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
		List<T> items = page.getContent()
			.stream()
			.map(mapper)
			.toList();

		return new PageResponse<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
	}

}
