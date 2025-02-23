package ncaaprs.ncaaprs_service;

import ncaaprs.ncaaprs_service.configuration.MongoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;



@SpringBootApplication
public class NcaaprsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NcaaprsServiceApplication.class, args);
	}



}