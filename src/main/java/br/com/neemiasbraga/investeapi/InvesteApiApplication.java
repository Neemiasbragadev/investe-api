package br.com.neemiasbraga.investeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InvesteApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvesteApiApplication.class, args);
	}

}
