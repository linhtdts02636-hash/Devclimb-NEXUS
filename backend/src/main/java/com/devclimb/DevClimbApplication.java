package com.devclimb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;

@SpringBootApplication(exclude = {
		ServletWebSecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class
})
public class DevClimbApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevClimbApplication.class, args);
	}

}
