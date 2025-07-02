package org.balanceus.topping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ToppingApplication {

	public static void main(String[] args) {

		// Dotenv dotenv = Dotenv.configure().load();
		// dotenv.entries().forEach(entry ->
		// 	System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(ToppingApplication.class, args);
	}

}
