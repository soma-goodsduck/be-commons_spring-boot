package com.ducks.goodsduck.commons;

import com.ducks.goodsduck.commons.config.FCMInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableScheduling
public class CommonsApplication {

	@Autowired
	private FCMInitializer fcmInitializer;

	public static void main(String[] args) {
		SpringApplication.run(CommonsApplication.class, args);
	}
}
