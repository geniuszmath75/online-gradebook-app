package org.example.onlinegradebookapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.onlinegradebookapp.entity")
@EnableJpaRepositories("org.example.onlinegradebookapp.repository")
public class OnlineGradebookAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineGradebookAppApplication.class, args);
	}

}
