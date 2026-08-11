package com.isc.cardmanagement;

import org.springframework.boot.SpringApplication;

public class TestCardManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CardManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
