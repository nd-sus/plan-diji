package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

	/*
	 * TODO - Senthil - Take the jar from target folder.
	 * Do mvn clean install -DskipTests=true to create the jar
	 */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
