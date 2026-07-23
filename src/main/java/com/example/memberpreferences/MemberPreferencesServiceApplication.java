package com.example.memberpreferences;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MemberPreferencesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberPreferencesServiceApplication.class, args);
	}

}
