package rdhxb.raportservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RaportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaportServiceApplication.class, args);
	}

}
