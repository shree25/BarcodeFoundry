package com.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.servlet.Filter;
import com.moesif.servlet.MoesifFilter;

@SpringBootApplication
public class SpringBootWebApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

	@Bean
	public Filter moesifFilter() {
		return new MoesifFilter("eyJhcHAiOiIxNTA6NTQ0IiwidmVyIjoiMi4wIiwib3JnIjoiNjkwOjE3NSIsImlhdCI6MTU2OTQ1NjAwMH0.jQcz9TrBuoLw6kb9gU3t7BOYvbQYM66LKqiY4lxtG2g");
	}

}