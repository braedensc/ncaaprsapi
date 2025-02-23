package ncaaprs.ncaaprs_service.controller;

import ncaaprs.ncaaprs_service.controller.dto.athleteDto;
import ncaaprs.ncaaprs_service.controller.dto.teamDto;
import ncaaprs.ncaaprs_service.controller.dto.teamResponseDto;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.service.AthletesService;
import ncaaprs.ncaaprs_service.service.ScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AthletesController {

	private final AthletesService athletesService;
	private final ScraperService scraperService;

	public AthletesController(AthletesService athleteService, ScraperService scraperService) {
		this.athletesService= athleteService;
		this.scraperService = scraperService;
	}

	@GetMapping("/athletes")
	public @ResponseBody String greeting() {
		return athletesService.getAthletes();
	}


	@GetMapping("/scrapeAndUpdateTeam")
	public @ResponseBody ResponseEntity<String> scrapeResponse(@RequestParam String teamLink) {
		return scraperService.scrapeAndUpdateTeam(teamLink);
	}


	@GetMapping("/team")
	public @ResponseBody ResponseEntity<teamResponseDto> fetchTeamResponse(@RequestParam String teamLink) {
		ResponseEntity<teamDto> teamResponse = athletesService.getTeam(teamLink);
		if (!teamResponse.getStatusCode().is2xxSuccessful() || teamResponse.getBody() == null) {
			return ResponseEntity.notFound().build(); // Return 404 if team response is not successful or team is null
		}
			teamDto team = teamResponse.getBody();
			ResponseEntity<List<athleteDto>> athletesResponse = athletesService.getActiveAthletesByTeamId(team.getId(), teamLink);
			if (!athletesResponse.getStatusCode().is2xxSuccessful()) {
				return ResponseEntity.notFound().build(); // Return 404 if athletes response is not successful or body is null
			}
		List<athleteDto> athletes = athletesResponse.getBody() != null ? athletesResponse.getBody() : new ArrayList<>();
		teamResponseDto responseData = new teamResponseDto();
		responseData.setAthletes(athletes);
		responseData.setTeam(team);
		return ResponseEntity.ok(responseData);
	}

}