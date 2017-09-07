package com.github.weiwei02.microservice.providemove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.github.weiwei02.microservice")
@EnableDiscoveryClient
public class ProviderMovieApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProviderMovieApplication.class, args);
	}
}
