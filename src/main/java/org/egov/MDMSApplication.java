package org.egov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;


@SpringBootApplication
@EnableCaching
public class MDMSApplication {

	public static void main(String[] args) {
		SpringApplication.run(MDMSApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaTemplate<String,String> kafkaTemplate){
		return args->{
			kafkaTemplate.send("mdms","hello kafka");
		};
	}

}
