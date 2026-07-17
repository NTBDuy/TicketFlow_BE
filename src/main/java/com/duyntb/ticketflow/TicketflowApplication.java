package com.duyntb.ticketflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class TicketflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketflowApplication.class, args);
	}

}
