package com.ducks.goodsduck.commons;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.config.location=" +
		"classpath:/application.yml," +
		"classpath:/application-db.yml," +
		"classpath:/application-oauth2.yml")
class CommonsApplicationTests {

	@Test
	void contextLoads() {
	}

}
