package com.tenpearls.contactmanagement;

import com.tenpearls.contactmanagement.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ContactManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactManagementSystemApplication.class, args);
	}

}
