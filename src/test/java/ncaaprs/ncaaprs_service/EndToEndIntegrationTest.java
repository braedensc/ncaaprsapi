package ncaaprs.ncaaprs_service;

import ncaaprs.ncaaprs_service.repository.AthleteRepository;
import ncaaprs.ncaaprs_service.repository.TeamRepository;
import ncaaprs.ncaaprs_service.service.ScraperService;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.nio.file.Files;
import java.nio.file.Paths;
import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;

public class EndToEndIntegrationTest extends MongoTestContainerConfig {

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private TeamRepository teamRepository;

    @MockBean
    private ncaaprs.ncaaprs_service.repository.ScraperClient scraperClient;

    private String loadJsonFromFile(String filename) throws Exception {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(filename).toURI())));
    }

    @Test
    public void testScrapeAndSaveAthletes() throws Exception {
        String sampleJson = loadJsonFromFile("athleteScraperResponseSample.json");
        when(scraperClient.scrapeTeam("https://www.tfrrs.org/teams/GA_college_m_Georgia_Tech.html"))
                .thenReturn(ResponseEntity.ok(sampleJson));

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam("https://www.tfrrs.org/teams/GA_college_m_Georgia_Tech.html");


        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Team savedTeam = teamRepository.findTeamIdByLink("https://www.tfrrs.org/teams/GA_college_m_Georgia_Tech.html").orElse(null);
        assertThat(savedTeam).isNotNull();
        assertThat(savedTeam.getTeamName()).isNotEmpty();
        assertThat(savedTeam.getLogo()).isNotEmpty();
        List<Athlete> savedAthletes = athleteRepository.getActiveAthletesByTeamId(savedTeam.getId());
        assertThat(savedAthletes).isNotEmpty();

        // Each athlete should have required fields
        for (Athlete athlete : savedAthletes) {
            assertThat(athlete.getName()).isNotEmpty();
            assertThat(athlete.getLink()).isNotEmpty();
            assertThat(athlete.getTeamLogo()).isNotEmpty();
            assertThat(athlete.getTeamTitle()).isNotEmpty();
        }
        // Athletes should belong to the correct team
        for (Athlete athlete : savedAthletes) {
            assertThat(athlete.getTeamId()).isEqualTo(savedTeam.getId());
        }
        // Each athlete should have a valid performance record list
        for (Athlete athlete : savedAthletes) {
            assertThat(athlete.getPrs()).isNotNull();
        }
        //At least one athlete should have valid PR records
        boolean hasValidPrs = savedAthletes.stream()
                .anyMatch(athlete -> athlete.getPrs() != null && !athlete.getPrs().isEmpty());
        assertThat(hasValidPrs).isTrue();
        //PR records should have valid structure
        for (Athlete athlete : savedAthletes) {
            athlete.getPrs().forEach(pr -> {
                assertThat(pr.getEventId()).isNotEmpty();
                assertThat(pr.getCurrentPr()).isNotEmpty();
            });
        }
        //No duplicate athlete names for the same team (ensures uniqueness)
        long distinctNamesCount = savedAthletes.stream().map(Athlete::getName).distinct().count();
        assertThat(distinctNamesCount).isEqualTo(savedAthletes.size());


        //Another exact PR check for a different athlete
        Athlete tristan = savedAthletes.stream()
                .filter(a -> a.getName().equals("Tristan Autry"))
                .findFirst()
                .orElse(null);

        assertThat(tristan).isNotNull();
        assertThat(tristan.getPrs()).hasSize(4);
        assertThat(tristan.getPrs()).extracting(PerformanceRecord::getEventId)
                .containsExactlyInAnyOrder("pr1500", "pr3000", "pr1000", "pr3000S");
        assertThat(tristan.getPrs()).extracting(PerformanceRecord::getCurrentPr)
                .containsExactlyInAnyOrder("3:59.76", "8:22.56", "2:36.47", "9:02.10");


        //*Check Sprinter (e.g., 100m, 200m, 400m)**
        Athlete sprinter = savedAthletes.stream()
                .filter(a -> a.getName().equals("Weston Baptiste")) // Change name based on dataset
                .findFirst()
                .orElse(null);

        assertThat(sprinter).isNotNull();
        assertThat(sprinter.getPrs()).hasSize(2);
        assertThat(sprinter.getPrs()).extracting(PerformanceRecord::getEventId)
                .containsExactlyInAnyOrder("pr200", "pr400");
        assertThat(sprinter.getPrs()).extracting(PerformanceRecord::getCurrentPr)
                .containsExactlyInAnyOrder("22.62  (1.7)", "49.44");

        // **Check Field Athlete (e.g., Long Jump, Shot Put)**
        Athlete fieldAthlete = savedAthletes.stream()
                .filter(a -> a.getName().equals("Charlie Crowder")) // Change name based on dataset
                .findFirst()
                .orElse(null);

        assertThat(fieldAthlete).isNotNull();
        assertThat(fieldAthlete.getPrs()).hasSize(2);
        assertThat(fieldAthlete.getPrs()).extracting(PerformanceRecord::getEventId)
                .containsExactlyInAnyOrder("prLJ", "prTJ");// Example event ID
        assertThat(fieldAthlete.getPrs()).extracting(PerformanceRecord::getCurrentPr)
                .contains("13.75m      45 1.5", "6.72m      22 0.75"); // Example PR

        //**Check Athlete Without Any PRs**
        Athlete noPrAthlete = savedAthletes.stream()
                .filter(a -> a.getName().equals("Elijah Forrest")) // Change name based on dataset
                .findFirst()
                .orElse(null);

        assertThat(noPrAthlete).isNotNull();
        assertThat(noPrAthlete.getPrs()).isEmpty(); // No PRs expected



    }
}

