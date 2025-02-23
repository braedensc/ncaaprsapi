package ncaaprs.ncaaprs_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ncaaprs.ncaaprs_service.constants.EventKeys;
import ncaaprs.ncaaprs_service.repository.AthleteRepository;
import ncaaprs.ncaaprs_service.repository.ScraperClient;
import ncaaprs.ncaaprs_service.repository.TeamRepository;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;
import ncaaprs.ncaaprs_service.repository.models.PrHistory;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.service.AthletesService;
import ncaaprs.ncaaprs_service.service.ScraperService;
import ncaaprs.ncaaprs_service.service.dto.ScrapedAthleteDto;
import ncaaprs.ncaaprs_service.service.dto.ScraperResponseDto;
import ncaaprs.ncaaprs_service.service.mapper.AthleteMapper;
import ncaaprs.ncaaprs_service.service.mapper.TeamMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class ScraperServiceTests {


    @Mock
    private ScraperClient scraperClient;


    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AthleteMapper athleteMapper;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ScraperService scraperService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testScrapeAndUpdateNewTeamSucceedsWhenScraperIsSuccessful() throws IOException {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setId("teamId");
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000);
        Athlete athlete = new Athlete();
        List<Athlete> mockAthletes = new ArrayList<>();
        mockAthletes.add(athlete);
        // Build mock scraper response built from real json sample
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("athleteScraperResponseSample.json");
        assertNotNull(inputStream, "File not found in resources");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(fileContent.toString());
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);

        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.empty());
        when(objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class)).thenReturn(scraperResponseDto);
        when(teamMapper.toEntity(scraperResponseDto.getTeam(), teamLink)).thenReturn(mockTeam);
        when(athleteMapper.toEntity(scraperResponseDto.getParsedAthletes().getFirst(), "teamId", null, null)).thenReturn(athlete);
        when(teamRepository.save(mockTeam)).thenReturn(mockTeam);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).saveAll(mockAthletes);

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(athleteRepository, times(1)).saveAll(anyList());
    }


    @Test
    void testScrapeAndUpdateExistingTeamSucceedsWhenScraperIsSuccessful() throws IOException {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000);
        Athlete athlete = new Athlete();
        athlete.setLink("athleteLink");
        List<Athlete> mockAthletes = new ArrayList<>();
        mockAthletes.add(athlete);
        // Build mock scraper response built from real json sample
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("athleteScraperResponseSample.json");
        assertNotNull(inputStream, "File not found in resources");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(fileContent.toString());
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);
        Team existingTeam = new Team();
        existingTeam.setId("teamId");
        existingTeam.setVersion(1L);
        existingTeam.setFailureCount(1);
        Athlete existingAthlete = new Athlete();
        existingAthlete.setLink("athleteLink");
        existingAthlete.setId("existingAthleteId");
        List<String> existingAthleteLinks = new ArrayList<>();
        existingAthleteLinks.add("athleteLink");



        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(existingTeam));
        when(objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class)).thenReturn(scraperResponseDto);
        when(teamMapper.toEntity(scraperResponseDto.getTeam(), teamLink)).thenReturn(mockTeam);
        when(athleteMapper.toEntity(scraperResponseDto.getParsedAthletes().getFirst(), "teamId", null, null)).thenReturn(athlete);
        when(teamRepository.save(mockTeam)).thenReturn(mockTeam);

        when(athleteRepository.getAthleteLinksByTeamId(existingTeam.getId())).thenReturn(existingAthleteLinks);
        when(athleteRepository.findByLink(existingAthleteLinks.getFirst())).thenReturn(existingAthlete);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).saveAll(mockAthletes);

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(athleteRepository, times(1)).saveAll(anyList());
        verify(athleteRepository, times(1)).getAthleteLinksByTeamId(existingTeam.getId());
        assertEquals(mockTeam.getId(), existingTeam.getId());
        assertEquals(mockTeam.getVersion(), existingTeam.getVersion());
    }



    @Test
    void testScrapeAndUpdateExistingTeamSetInactiveAthletesSucceedsWhenScraperIsSuccessful() throws IOException {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000);
        Athlete athlete = new Athlete();
        athlete.setLink("athleteLink");
        List<Athlete> mockAthletes = new ArrayList<>();
        mockAthletes.add(athlete);
        // Build mock scraper response built from real json sample
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("athleteScraperResponseSample.json");
        assertNotNull(inputStream, "File not found in resources");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(fileContent.toString());
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);
        Team existingTeam = new Team();
        existingTeam.setId("teamId");
        existingTeam.setVersion(1L);
        existingTeam.setFailureCount(1);
        Athlete existingAthlete = new Athlete();
        existingAthlete.setLink("athleteLinkOld");
        existingAthlete.setId("existingAthleteId");
        existingAthlete.setActive(true);
        List<String> existingAthleteLinks = new ArrayList<>();
        existingAthleteLinks.add("athleteLink");

        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(existingTeam));
        when(objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class)).thenReturn(scraperResponseDto);
        when(teamMapper.toEntity(scraperResponseDto.getTeam(), teamLink)).thenReturn(mockTeam);
        when(athleteMapper.toEntity(scraperResponseDto.getParsedAthletes().getFirst(), "teamId", null, null)).thenReturn(athlete);
        when(teamRepository.save(mockTeam)).thenReturn(mockTeam);

        when(athleteRepository.getAthleteLinksByTeamId(existingTeam.getId())).thenReturn(existingAthleteLinks);
        when(athleteRepository.findByLink(existingAthleteLinks.getFirst())).thenReturn(existingAthlete);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).saveAll(mockAthletes);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).markAthletesAsInactiveByLinks(existingAthleteLinks);

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(athleteRepository, times(1)).saveAll(anyList());
        verify(athleteRepository, times(1)).getAthleteLinksByTeamId(existingTeam.getId());
        verify(athleteRepository, times(1)).markAthletesAsInactiveByLinks(existingAthleteLinks);
        assertEquals(mockTeam.getId(), existingTeam.getId());
        assertEquals(mockTeam.getVersion(), existingTeam.getVersion());
    }


    @Test
    void testScrapeAndUpdateExistingTeamAthletePrsUpdateWithSameEventSucceedsWhenScraperIsSuccessful() throws IOException {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000);
        Athlete athlete = new Athlete();
        athlete.setLink("athleteLink");
        PerformanceRecord performanceRecord = new PerformanceRecord();
        performanceRecord.setEventId(String.valueOf(EventKeys.pr1500));
        performanceRecord.setCurrentPr("3:45");
        List<PerformanceRecord> allPrsForAthlete = new ArrayList<>();
        allPrsForAthlete.add(performanceRecord);
        athlete.setPrs(allPrsForAthlete);
        List<Athlete> mockAthletes = new ArrayList<>();
        mockAthletes.add(athlete);
        // Build mock scraper response built from real json sample
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("athleteScraperResponseSample.json");
        assertNotNull(inputStream, "File not found in resources");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(fileContent.toString());
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);
        Team existingTeam = new Team();
        existingTeam.setId("teamId");
        existingTeam.setVersion(1L);
        existingTeam.setFailureCount(1);
        Athlete existingAthlete = new Athlete();
        existingAthlete.setLink("athleteLink");
        existingAthlete.setId("existingAthleteId");
        PrHistory prHistory = new PrHistory(System.currentTimeMillis() - 605_000_000, "3:52");
        List<PrHistory> prHistories = new ArrayList<>();
        prHistories.add(prHistory);
        PerformanceRecord performanceRecordExiting = new PerformanceRecord();
        performanceRecordExiting.setEventId(String.valueOf(EventKeys.pr1500));
        performanceRecordExiting.setCurrentPr("3:52");
        List<PerformanceRecord> allPrsForExistingAthlete = new ArrayList<>();
        allPrsForExistingAthlete.add(performanceRecordExiting);
        existingAthlete.setPrs(allPrsForExistingAthlete);
        List<String> existingAthleteLinks = new ArrayList<>();
        existingAthleteLinks.add("athleteLink");

        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(existingTeam));
        when(objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class)).thenReturn(scraperResponseDto);
        when(teamMapper.toEntity(scraperResponseDto.getTeam(), teamLink)).thenReturn(mockTeam);
        when(athleteMapper.toEntity(scraperResponseDto.getParsedAthletes().getFirst(), "teamId", null, null)).thenReturn(athlete);
        when(teamRepository.save(mockTeam)).thenReturn(mockTeam);

        when(athleteRepository.getAthleteLinksByTeamId(existingTeam.getId())).thenReturn(existingAthleteLinks);
        when(athleteRepository.findByLink(existingAthleteLinks.getFirst())).thenReturn(existingAthlete);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).saveAll(mockAthletes);

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(athleteRepository, times(1)).saveAll(anyList());
        verify(athleteRepository, times(1)).getAthleteLinksByTeamId(existingTeam.getId());
        assertEquals(mockTeam.getId(), existingTeam.getId());
        assertEquals(mockTeam.getVersion(), existingTeam.getVersion());
        assertEquals(athlete.getPrs(), existingAthlete.getPrs());
        assertEquals(athlete.getPrs().getFirst().getCurrentPr(), "3:45");
        assertEquals(existingAthlete.getPrs().getFirst().getCurrentPr(), "3:45");
        assertEquals(athlete.getPrs().getFirst().getHistory().getFirst().getPr(), "3:52" );
        assertEquals(athlete.getPrs().getFirst().getHistory().size(), 1 );
        assertEquals(athlete.getPrs().size(), 1 );
        assertEquals(athlete.getPrs().getFirst().getEventId(), String.valueOf(EventKeys.pr1500));
    }




    @Test
    void testScrapeAndUpdateExistingTeamAthletePrsUpdateWithNewEventSucceedsWhenScraperIsSuccessful() throws IOException {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000);
        Athlete athlete = new Athlete();
        athlete.setLink("athleteLink");
        PerformanceRecord performanceRecord = new PerformanceRecord();
        performanceRecord.setEventId(String.valueOf(EventKeys.pr5000));
        performanceRecord.setCurrentPr("15:00");
        List<PerformanceRecord> allPrsForAthlete = new ArrayList<>();
        allPrsForAthlete.add(performanceRecord);
        athlete.setPrs(allPrsForAthlete);
        List<Athlete> mockAthletes = new ArrayList<>();
        mockAthletes.add(athlete);
        // Build mock scraper response built from real json sample
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("athleteScraperResponseSample.json");
        assertNotNull(inputStream, "File not found in resources");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            fileContent.append(line);
        }
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(fileContent.toString());
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);
        Team existingTeam = new Team();
        existingTeam.setId("teamId");
        existingTeam.setVersion(1L);
        existingTeam.setFailureCount(1);
        Athlete existingAthlete = new Athlete();
        existingAthlete.setLink("athleteLink");
        existingAthlete.setId("existingAthleteId");
        PrHistory prHistory = new PrHistory(System.currentTimeMillis() - 605_000_000, "3:52");
        List<PrHistory> prHistories = new ArrayList<>();
        prHistories.add(prHistory);
        PerformanceRecord performanceRecordExiting = new PerformanceRecord();
        performanceRecordExiting.setEventId(String.valueOf(EventKeys.pr1500));
        performanceRecordExiting.setCurrentPr("3:52");
        performanceRecordExiting.setHistory(prHistories);
        List<PerformanceRecord> allPrsForExistingAthlete = new ArrayList<>();
        allPrsForExistingAthlete.add(performanceRecordExiting);
        existingAthlete.setPrs(allPrsForExistingAthlete);
        List<String> existingAthleteLinks = new ArrayList<>();
        existingAthleteLinks.add("athleteLink");



        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(existingTeam));
        when(objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class)).thenReturn(scraperResponseDto);
        when(teamMapper.toEntity(scraperResponseDto.getTeam(), teamLink)).thenReturn(mockTeam);
        when(athleteMapper.toEntity(scraperResponseDto.getParsedAthletes().getFirst(), "teamId", null, null)).thenReturn(athlete);
        when(teamRepository.save(mockTeam)).thenReturn(mockTeam);

        when(athleteRepository.getAthleteLinksByTeamId(existingTeam.getId())).thenReturn(existingAthleteLinks);
        when(athleteRepository.findByLink(existingAthleteLinks.getFirst())).thenReturn(existingAthlete);
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(athleteRepository).saveAll(mockAthletes);

        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(athleteRepository, times(1)).saveAll(anyList());
        verify(athleteRepository, times(1)).getAthleteLinksByTeamId(existingTeam.getId());
        assertEquals(mockTeam.getId(), existingTeam.getId());
        assertEquals(mockTeam.getVersion(), existingTeam.getVersion());
        assertEquals(athlete.getPrs(), existingAthlete.getPrs());
        assertEquals(athlete.getPrs().size(), 2 );
        assertEquals(athlete.getPrs().getFirst().getEventId(), String.valueOf(EventKeys.pr1500));
        assertEquals(athlete.getPrs().getFirst().getCurrentPr(), "3:52");
        assertEquals(athlete.getPrs().getFirst().getHistory().getFirst().getPr(), "3:52" );
        assertEquals(athlete.getPrs().get(1).getEventId(), String.valueOf(EventKeys.pr5000));
        assertEquals(athlete.getPrs().get(1).getCurrentPr(), "15:00");
        assertEquals(athlete.getPrs().get(1).getHistory().getFirst().getPr(), "15:00");


    }


    @Test
    void testScrapeAndUpdateWhenScraperFailsReturnScraperError() throws IOException {
        String teamLink = "teamLink";
        ResponseEntity<String> scraperResponse = ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        ScraperResponseDto scraperResponseDto = new ScraperResponseDto();
        List<ScrapedAthleteDto> scrapedAthletes = new ArrayList<>();
        scrapedAthletes.add(new ScrapedAthleteDto());
        scraperResponseDto.setParsedAthletes(scrapedAthletes);
        Team existingTeam = new Team();
        existingTeam.setId("teamId");
        existingTeam.setVersion(1L);
        existingTeam.setFailureCount(1);
        Athlete existingAthlete = new Athlete();
        existingAthlete.setLink("athleteLink");
        existingAthlete.setId("existingAthleteId");
        PrHistory prHistory = new PrHistory(System.currentTimeMillis() - 605_000_000, "3:52");
        List<PrHistory> prHistories = new ArrayList<>();
        prHistories.add(prHistory);
        PerformanceRecord performanceRecordExiting = new PerformanceRecord();
        performanceRecordExiting.setEventId(String.valueOf(EventKeys.pr1500));
        performanceRecordExiting.setCurrentPr("3:52");
        performanceRecordExiting.setHistory(prHistories);
        List<PerformanceRecord> allPrsForExistingAthlete = new ArrayList<>();
        allPrsForExistingAthlete.add(performanceRecordExiting);
        existingAthlete.setPrs(allPrsForExistingAthlete);

        when(scraperClient.scrapeTeam(teamLink)).thenReturn(scraperResponse);
        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(existingTeam));
        // Mock the saveAll method to do nothing (since the return type is void)
        doNothing().when(teamRepository).incrementFailureCount(teamLink);


        ResponseEntity<String> response = scraperService.scrapeAndUpdateTeam(teamLink);
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(scraperClient, times(1)).scrapeTeam(teamLink);
        verify(teamRepository, times(1)).findTeamIdByLink(teamLink);
        verify(teamRepository, times(1)).incrementFailureCount("teamId");


    }
}
