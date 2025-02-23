package ncaaprs.ncaaprs_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean(name="webScraperHttpClient")
    public RestClient restClient() {
        return RestClient.builder().baseUrl("http://127.0.0.1:5000/api").build();
    }

}


