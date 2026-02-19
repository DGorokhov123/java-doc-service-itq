package ru.dgorokhov.docservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocserviceApplication.class, args);
	}

}
