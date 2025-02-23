package ncaaprs.ncaaprs_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ncaaprs.ncaaprs_service.constants.EventKeys;
import ncaaprs.ncaaprs_service.controller.dto.teamDto;
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

class AthletesServiceTests {

    @Mock
    private ScraperClient scraperClient;


    @Mock
    private TeamRepository teamRepository;



    @Mock
    ScraperService scraperService;


    @Mock
    private TeamMapper teamMapper;


    @InjectMocks
    private AthletesService athletesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTeamReturnsExistingTeamWhenRecentAndNotFailing() {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setId("teamId");
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis());
        teamDto mockTeamDto = new teamDto();
        mockTeamDto.setId("teamId");

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(mockTeam));
        when(teamMapper.toDto(mockTeam)).thenReturn(mockTeamDto);

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("teamId", response.getBody().getId());
        verify(teamRepository, times(1)).findTeamIdByLink(teamLink);
        verifyNoInteractions(scraperClient);
    }

    @Test
    void testGetTeamScrapeSucceedsWhenTeamIsOutdatedAndReturnsTeam() {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setId("teamId");
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000); // Outdated
        teamDto mockTeamDto = new teamDto();
        mockTeamDto.setId("teamId");
        // Mock updated team (after scraping)
        Team mockUpdatedTeam = new Team();
        mockUpdatedTeam.setId("teamId");
        mockUpdatedTeam.setFailureCount(0);
        mockUpdatedTeam.setLastUpdatedOn(System.currentTimeMillis()); // Current time (fresh data)

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(mockTeam)).thenReturn(Optional.of(mockUpdatedTeam));
        when(scraperService.scrapeAndUpdateTeam(teamLink)).thenReturn(ResponseEntity.ok("{\"team\": {\"id\": \"teamId\"}}"));
        when(teamMapper.toDto(mockUpdatedTeam)).thenReturn(mockTeamDto);

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("teamId", response.getBody().getId());
        verify(scraperService, times(1)).scrapeAndUpdateTeam(teamLink);
        // Verifying that we got the updated team (check timestamp) and not the stale data
        assertTrue(mockUpdatedTeam.getLastUpdatedOn() > mockTeam.getLastUpdatedOn(),
                "The lastUpdatedOn timestamp should be updated after the scrape.");

    }


    @Test
    void testGetTeamDoesNotScrapeWhenTeamIsOutdatedAndFailingAndReturnsStaleTeam() {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setId("teamId");
        mockTeam.setFailureCount(3); //failed 3 previous scrapes
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000); // Outdated
        teamDto mockTeamDto = new teamDto();
        mockTeamDto.setId("teamId");

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(mockTeam));
        when(teamMapper.toDto(mockTeam)).thenReturn(mockTeamDto);
        when(teamMapper.toDto(mockTeam)).thenReturn(mockTeamDto);

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("teamId", response.getBody().getId());
        verify(teamRepository, times(1)).findTeamIdByLink(teamLink);
        verify(scraperService, times(0)).scrapeAndUpdateTeam(teamLink);
        verifyNoInteractions(scraperClient);
    }

    @Test
    void testGetTeamScrapeFailsWhenTeamIsOutdatedAndReturnsStaleTeam() {
        String teamLink = "teamLink";
        Team mockTeam = new Team();
        mockTeam.setId("teamId");
        mockTeam.setFailureCount(0);
        mockTeam.setLastUpdatedOn(System.currentTimeMillis() - 605_000_000); // Outdated
        teamDto mockTeamDto = new teamDto();
        mockTeamDto.setId("teamId");
        // Mock updated team (after scraping)
        Team mockUpdatedTeam = new Team();
        mockUpdatedTeam.setId("teamId");
        mockUpdatedTeam.setFailureCount(0);
        mockUpdatedTeam.setLastUpdatedOn(System.currentTimeMillis()); // Current time (fresh data)

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.of(mockTeam)).thenReturn(Optional.of(mockTeam));
        when(scraperService.scrapeAndUpdateTeam(teamLink)).thenReturn(ResponseEntity.internalServerError().body(null));
        when(teamMapper.toDto(mockTeam)).thenReturn(mockTeamDto);

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("teamId", response.getBody().getId());
        verify(scraperService, times(1)).scrapeAndUpdateTeam(teamLink);

    }


    @Test
    void testGetTeamScrapeSucceedsWhenTeamIsMissingAndReturnsTeam() {
        String teamLink = "teamLink";
        Team mockTeam = null;
        teamDto mockTeamDto = new teamDto();
        mockTeamDto.setId("teamId");
        // Mock updated team (after scraping)
        Team mockUpdatedTeam = new Team();
        mockUpdatedTeam.setId("teamId");
        mockUpdatedTeam.setFailureCount(0);
        mockUpdatedTeam.setLastUpdatedOn(System.currentTimeMillis()); // Current time (fresh data)

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.empty()).thenReturn(Optional.of(mockUpdatedTeam));
        when(scraperService.scrapeAndUpdateTeam(teamLink)).thenReturn(ResponseEntity.ok("{\"team\": {\"id\": \"teamId\"}}"));
        when(teamMapper.toDto(mockUpdatedTeam)).thenReturn(mockTeamDto);

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("teamId", response.getBody().getId());
        verify(scraperService, times(1)).scrapeAndUpdateTeam(teamLink);

    }

    @Test
    void testGetTeamScrapeFailsWhenTeamIsMissingAndReturnsError() {
        String teamLink = "teamLink";

        when(teamRepository.findTeamIdByLink(teamLink)).thenReturn(Optional.empty());
        when(scraperService.scrapeAndUpdateTeam(teamLink)).thenReturn(ResponseEntity.internalServerError().body(null));

        ResponseEntity<teamDto> response = athletesService.getTeam(teamLink);
        //returns status code that scraper returns
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(scraperService, times(1)).scrapeAndUpdateTeam(teamLink);
    }





}
