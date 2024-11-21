package cap.team3.what;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class WhatApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatApplication.class, args);
	}

}
