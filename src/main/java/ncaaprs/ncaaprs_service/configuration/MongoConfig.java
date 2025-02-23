package ncaaprs.ncaaprs_service.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;



@Configuration
public class MongoConfig {


    @Value("${sm://ncaaprsmongoconnectionstring}")
    private  String mongoConnectionString;


    @Bean
    public MongoClient mongoClient() {
        System.out.println("🔍 Resolved MongoDB URI: " + mongoConnectionString);
        return MongoClients.create(mongoConnectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "appdata");
    }
}
