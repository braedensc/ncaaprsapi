package ncaaprs.ncaaprs_service.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Repository
public class ScraperClient {

    private static final Logger log = LoggerFactory.getLogger(ScraperClient.class);
    private final RestTemplate restTemplate;

    public ScraperClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> scrapeTeam(String teamLink) {
        log.info("Fetching details for teamLink: {}", teamLink);
        try {
            String uri = UriComponentsBuilder.fromHttpUrl("http://localhost:3000/api/athletes/")
                    .queryParam("param1", teamLink)
                    .toUriString();
            log.info("Making GET request to URI: {}", uri);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RestTemplate");
            headers.set("Accept", "*/*");
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            log.info("Received response with status: {}", response.getStatusCode());
            return response;
        } catch (HttpStatusCodeException ex) {
            return handleHttpStatus(ex);
        } catch (Exception ex) {
            log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred");
        }
    }

    private ResponseEntity<String> handleHttpStatus(HttpStatusCodeException ex) {
        return switch (ex.getStatusCode()) {
            case HttpStatus.NOT_FOUND -> ResponseEntity.status(ex.getStatusCode()).body("Resource not found");
            case HttpStatus.BAD_REQUEST -> ResponseEntity.status(ex.getStatusCode()).body("Invalid request");
            case HttpStatus.UNAUTHORIZED -> ResponseEntity.status(ex.getStatusCode()).body("Unauthorized access");
            case HttpStatus.FORBIDDEN -> ResponseEntity.status(ex.getStatusCode()).body("Forbidden access");
            case HttpStatus.INTERNAL_SERVER_ERROR -> ResponseEntity.status(ex.getStatusCode()).body("Server encountered an error");
            default -> ResponseEntity.status(ex.getStatusCode()).body("An error occurred: " + ex.getStatusText());
        };
    }


}

