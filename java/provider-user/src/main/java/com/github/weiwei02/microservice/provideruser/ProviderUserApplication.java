package com.github.weiwei02.microservice.provideruser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages={"com.github.weiwei02.microservice.provideruser"})
public class ProviderUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProviderUserApplication.class, args);
	}
}
