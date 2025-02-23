package ncaaprs.ncaaprs_service.service;


import ncaaprs.ncaaprs_service.controller.dto.athleteDto;
import ncaaprs.ncaaprs_service.controller.dto.teamDto;
import ncaaprs.ncaaprs_service.repository.AthleteRepository;
import ncaaprs.ncaaprs_service.repository.ScraperClient;
import ncaaprs.ncaaprs_service.repository.TeamRepository;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.service.mapper.AthleteMapper;
import ncaaprs.ncaaprs_service.service.mapper.TeamMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AthletesService {

    private static final Logger log = LoggerFactory.getLogger(ScraperClient.class);
    private final AthleteMapper athleteMapper;
    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;
    private final AthleteRepository athleteRepository;
    private final ScraperService scraperService;

    public AthletesService(AthleteMapper athleteMapper, TeamMapper teamMapper, TeamRepository teamRepository, AthleteRepository athleteRepository, ScraperService scraperService) {
        this.athleteMapper = athleteMapper;
        this.teamMapper = teamMapper;
        this.teamRepository = teamRepository;
        this.athleteRepository = athleteRepository;
        this.scraperService = scraperService;
    }

    public String getAthletes() {
        return "Here are your athletes";
    }


    @Transactional
    public ResponseEntity<teamDto> getTeam(String teamLink) {
        long scrapeStaleTeamThreshold = 604_800_000; //if team data is older than a week, let's attempt a scrape now
        Optional<Team> existingTeam = teamRepository.findTeamIdByLink(teamLink);
        Team team = existingTeam.orElse(null);
        //only return team immediately if: recent, not failing, and existing
        if (team != null && team.getFailureCount() < 3 && ((System.currentTimeMillis() - team.getLastUpdatedOn()) < scrapeStaleTeamThreshold)) {
            log.info("Team successsfully found and returned for teamLink from the db, no scrape performed: {}", teamLink);
            return ResponseEntity.ok(teamMapper.toDto(team));
        }
        //if team has failed more than 3 times, we will stop attempting to scrape and simply use stale data, as likely web scraper code needs updating
        if (team != null && team.getFailureCount() >= 3) {
            log.info("Team successsfully found and returned for teamLink from the db, no scrape performed due to team failureCount > 3: {}", teamLink);
            return ResponseEntity.ok(teamMapper.toDto(team));
        }
        //otherwise we should go ahead and attempt a scrape, and return the updated team info, if it was found
        log.info("Team data is outdated or missing. Initiating scrape-and-update for teamLink: {}", teamLink);
        ResponseEntity<String> scrapeResponse = scraperService.scrapeAndUpdateTeam(teamLink);
        //if scraper failed, try to return existing stale team data or error if team doesn't exist
        if (!scrapeResponse.getStatusCode().is2xxSuccessful()) {
            if (team != null) {
                log.error("Failed to update team data for teamLink: {}. Returning existing data.", teamLink);
                return ResponseEntity.status(200).body(teamMapper.toDto(team));
            } else {
                log.error("Failed to update team data for teamLink: {}.", teamLink);
                return ResponseEntity.status(scrapeResponse.getStatusCode()).body(null);
            }
        }
        //if scrape was successful, retrieve the newly added team info
        existingTeam = teamRepository.findTeamIdByLink(teamLink);
        team = existingTeam.orElse(null);
        if (team != null) {
            log.info("Team successsfully scraped and updated and returned for teamLink: {}", teamLink);
            return ResponseEntity.ok(teamMapper.toDto(team));
        }
        log.info("Unknown Error occured while fetching team after a scrape, but scraper has success response: {}", teamLink);
        return ResponseEntity.status(500).body(null);

    }

    public  ResponseEntity<List<athleteDto>> getActiveAthletesByTeamId(String teamId, String teamLink) {
        List<Athlete> foundAthletes = athleteRepository.getActiveAthletesByTeamId(teamId);
        if (foundAthletes == null || foundAthletes.isEmpty()) {
            log.info("No athletes found for team: {}", teamLink);
            foundAthletes = new ArrayList<>();
        }
        return ResponseEntity.ok(foundAthletes.stream().map(athlete -> athleteMapper.toDto(athlete)).toList());
    }
}