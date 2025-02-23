package ncaaprs.ncaaprs_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ncaaprs.ncaaprs_service.repository.AthleteRepository;
import ncaaprs.ncaaprs_service.repository.ScraperClient;
import ncaaprs.ncaaprs_service.repository.TeamRepository;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;
import ncaaprs.ncaaprs_service.repository.models.PrHistory;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.service.dto.ScrapedAthleteDto;
import ncaaprs.ncaaprs_service.service.dto.ScraperResponseDto;
import ncaaprs.ncaaprs_service.service.mapper.AthleteMapper;
import ncaaprs.ncaaprs_service.service.mapper.TeamMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperClient.class);
    private final ScraperClient scraperClient;
    private final ObjectMapper objectMapper;
    private final AthleteMapper athleteMapper;
    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;
    private final AthleteRepository athleteRepository;

    public ScraperService(ScraperClient scraperClient, ObjectMapper objectMapper, AthleteMapper athleteMapper, TeamMapper teamMapper, TeamRepository teamRepository, AthleteRepository athleteRepository) {
        this.scraperClient = scraperClient;
        this.objectMapper = objectMapper;
        this.athleteMapper = athleteMapper;
        this.teamMapper = teamMapper;
        this.teamRepository = teamRepository;
        this.athleteRepository = athleteRepository;
    }


    @Transactional
    public ResponseEntity<String> scrapeAndUpdateTeam(String teamLink) {
//step 1: call web scraper api to perform scrape of tffrs link given
        ResponseEntity<String> scraperResponse = scraperClient.scrapeTeam(teamLink);
        ScraperResponseDto athleteData;
        Optional<Team> existingTeamIdAndVersion = teamRepository.findTeamIdByLink(teamLink);
        //deserialize the scraper response, custom json deserialization logic is in scraperResponseDto class
        if (scraperResponse.getStatusCode().is2xxSuccessful() && scraperResponse.getBody() != null) {
            try {
                athleteData = objectMapper.readValue(scraperResponse.getBody(), ScraperResponseDto.class);
                log.info("Response after deserializing (scraperResponseDto) {}", athleteData);
            } catch (IOException e) {
                if (existingTeamIdAndVersion.isPresent()) {
                    teamRepository.incrementFailureCount(String.valueOf(existingTeamIdAndVersion.get().getId()));
                    log.error("Failed to deserialize the web scraper response body for existing team error: {}, response body: {}, teamLink: {}, teamId {}", e, scraperResponse.getBody(), teamLink, existingTeamIdAndVersion.get().getId());
                } else {
                    log.error("Failed to deserialize the web scraper response body for new team error: {}, response body: {}, teamLink: {}", e, scraperResponse.getBody(), teamLink);
                }
                return ResponseEntity.status(scraperResponse.getStatusCode()).body(null);
            }
            //step 2:perform update/insert of existing team and users using scraped data
            if (existingTeamIdAndVersion.isPresent()) {
                Team existingTeam = existingTeamIdAndVersion.get();
                String existingTeamId = existingTeam.getId();
                long existingTeamVersion = existingTeam.getVersion();

                Team updatedTeam = teamMapper.toEntity(athleteData.getTeam(), teamLink);
                updatedTeam.setId(existingTeamId);
                updatedTeam.setFailureCount(0);
                updatedTeam.setVersion(existingTeamVersion);
                //TODO: use the return value in response of this method, instead of returning the raw scraped data
                teamRepository.save(updatedTeam);
                List<String> existingAthleteLinks = athleteRepository.getAthleteLinksByTeamId(String.valueOf(existingTeamId));
                //update or insert all athletes that were freshly scraped
                List<Athlete> athletesToSave = new ArrayList<>();
                for (ScrapedAthleteDto scrapedAthlete : athleteData.getParsedAthletes()) {
                    Athlete newAthlete = athleteMapper.toEntity(scrapedAthlete, String.valueOf(existingTeamId), athleteData.getTeam().getLogo(), athleteData.getTeam().getTitle());

                    //update performance record and history of each athlete
                    if (existingAthleteLinks.contains(newAthlete.getLink())) {
                        Athlete existingAthlete = athleteRepository.findByLink(newAthlete.getLink());
                        updatePrsForAthlete(existingAthlete, newAthlete);
                        newAthlete.setId(existingAthlete.getId());
                    }
                    athletesToSave.add(newAthlete);
                }
                //TODO: this should return what was saved and include in response dto
                athleteRepository.saveAll(athletesToSave);

                //set inactive flag for athletes not found in recent scrape, but which were active previously
                List<String> scrapedAthleteLinks = athleteData.getParsedAthletes()
                        .stream()
                        .map(ScrapedAthleteDto::getLink)
                        .toList();
                List<String> athletesNoLongerActive = existingAthleteLinks.stream()
                        .filter(link -> !scrapedAthleteLinks.contains(link))
                        .toList();
                athleteRepository.markAthletesAsInactiveByLinks(athletesNoLongerActive);

                //step 2 (alt): perform insert of new team and athletes
            } else {
                //insert new team
                Team newTeam = teamMapper.toEntity(athleteData.getTeam(), teamLink);
                newTeam = teamRepository.save(newTeam);
                //insert new athletes
                List<Athlete> athletesToInsert = new ArrayList<>();
                for (ScrapedAthleteDto scrapedAthlete : athleteData.getParsedAthletes()) {
                    Athlete newAthlete = athleteMapper.toEntity(scrapedAthlete, String.valueOf(newTeam.getId()), athleteData.getTeam().getLogo(), athleteData.getTeam().getTitle());
                    athletesToInsert.add(newAthlete);
                }
                athleteRepository.saveAll(athletesToInsert);
            }
            return ResponseEntity.ok(scraperResponse.getBody());
            //if scraper failed update failure count for the given teamLink in db, if it exists and then throw error
        } else {
            if (existingTeamIdAndVersion.isPresent()) {
                teamRepository.incrementFailureCount(String.valueOf(existingTeamIdAndVersion.get().getId()));
                log.error("Request failed to scrape existing team with status code: {} and teamLink: {}, teamId: {}", scraperResponse.getStatusCode(), teamLink, existingTeamIdAndVersion.get().getId());
            } else {
                log.error("Request failed to scrape new team with status code: {} and teamLink: {}", scraperResponse.getStatusCode(), teamLink);
            }
            return ResponseEntity.status(scraperResponse.getStatusCode()).body(null);
        }
    }



    private static void updatePrsForAthlete(Athlete existingAthlete, Athlete newAthlete) {
        List<PerformanceRecord> existingRecords = existingAthlete.getPrs();
        for (PerformanceRecord newRecord : newAthlete.getPrs()) {
            boolean found = false;
            for (PerformanceRecord existingRecord : existingRecords) {
                if (existingRecord.getEventId().equals(newRecord.getEventId())) {
                    found = true;
                    // If the record already exists, compare and see if it's different
                    if (!existingRecord.getCurrentPr().equals(newRecord.getCurrentPr())) {
                        // If the PR is different, update the record with the new one
                        existingRecord.getHistory().addFirst(new PrHistory(System.currentTimeMillis(), existingRecord.getCurrentPr()));
                        existingRecord.setCurrentPr(newRecord.getCurrentPr());
                    }
                }
            }
            //new pr for event never before done by athlete
            if (!found) {
                PerformanceRecord newPerf = new PerformanceRecord();
                newPerf.setEventId(newRecord.getEventId());
                newPerf.setCurrentPr(newRecord.getCurrentPr());
                PrHistory newPrHistory = new PrHistory(System.currentTimeMillis(), newRecord.getCurrentPr());
                List<PrHistory> newPrHistoryList = new ArrayList<>();
                newPrHistoryList.add(newPrHistory);
                newPerf.setHistory(newPrHistoryList);
                existingRecords.add(newPerf);
            }
        }
        newAthlete.setPrs(existingAthlete.getPrs());
    }
}
