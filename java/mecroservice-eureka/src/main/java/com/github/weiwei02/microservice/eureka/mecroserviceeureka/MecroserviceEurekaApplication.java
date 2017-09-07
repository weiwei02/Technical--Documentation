package com.github.weiwei02.microservice.eureka.mecroserviceeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MecroserviceEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MecroserviceEurekaApplication.class, args);
	}
}
