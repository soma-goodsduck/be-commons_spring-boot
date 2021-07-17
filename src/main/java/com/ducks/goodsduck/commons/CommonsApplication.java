package com.ducks.goodsduck.commons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CommonsApplication {

	private static final String PROPERTIES =
					"spring.config.location=" +
					"classpath:/application.yml," +
//					"classpath:/application-localh2.yml," +
					"classpath:/application-localmysql.yml," +
					"classpath:/application-file.yml," +
					"classpath:/application-oauth2.yml";

	public static void main(String[] args) {

//		SpringApplication.run(CommonsApplication.class, args);
		new SpringApplicationBuilder(CommonsApplication.class)
				.properties(PROPERTIES)
				.run(args);
	}
}
