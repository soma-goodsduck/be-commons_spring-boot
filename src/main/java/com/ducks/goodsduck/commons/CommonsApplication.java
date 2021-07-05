package com.ducks.goodsduck.commons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class CommonsApplication {

	// Properties 파일 추가
	private static final List<String> PROPERTIES = new ArrayList<>(
			Arrays.asList(
					"classpath:/application.yml",
					"classpath:/application-db.yml",
					"classpath:/application-oauth2.yml"
			)
	);

	public static void main(String[] args) {
		final String PROPERTIES_LOCATION = "spring.config.location=";

		// 어플리케이션 환경변수에 Properties 추가 (설정 파일 분리 목적)
		String properties = PROPERTIES_LOCATION.concat(
				PROPERTIES
					.stream()
					.collect(Collectors.joining(","))
		);

		new SpringApplicationBuilder(CommonsApplication.class)
				.properties(properties)
				.run(args);

	}


}
