package ncaaprs.ncaaprs_service;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ncaaprs.ncaaprs_service.controller.AthletesController;
import ncaaprs.ncaaprs_service.controller.dto.athleteDto;
import ncaaprs.ncaaprs_service.controller.dto.teamDto;
import ncaaprs.ncaaprs_service.service.AthletesService;
import ncaaprs.ncaaprs_service.service.ScraperService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(AthletesController.class)
class AthletesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AthletesService service;

    @MockBean
    private ScraperService scraperService;

    @Test
    void greetingShouldReturnMessageFromService() throws Exception {
        when(service.getAthletes()).thenReturn("Here are your athletes");
        this.mockMvc.perform(get("/api/v1/athletes")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("athletes")));
    }
    @Test
    void fetchTeamResponseShouldReturnSuccess() throws Exception {
        teamDto teamMock = new teamDto();
        teamMock.setId("teamId");
        ResponseEntity<teamDto> teamResponse = ResponseEntity.ok(teamMock);
        List<athleteDto> athletes = new ArrayList<>();
        athletes.add(new athleteDto());
        athletes.add(new athleteDto());
        ResponseEntity<List<athleteDto>> athletesResponse = ResponseEntity.ok(athletes);

        when(service.getTeam("teamLink")).thenReturn(teamResponse);
        when(service.getActiveAthletesByTeamId("teamId", "teamLink")).thenReturn(athletesResponse);

        this.mockMvc.perform(get("/api/v1/team")
                        .param("teamLink", "teamLink"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"teamId\"")))
                .andExpect(jsonPath("$.athletes", org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    void fetchTeamResponseWhenNoTeamFoundShouldReturnNotFound() throws Exception {
        teamDto teamMock = new teamDto();
        teamMock.setId("teamId");
        ResponseEntity<teamDto> teamResponse = ResponseEntity.ok(null);
        when(service.getTeam("teamLink")).thenReturn(teamResponse);
        this.mockMvc.perform(get("/team")
                        .param("teamLink", "teamLink"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    @Test
    void fetchTeamResponseWhenTeamReturnsErrorShouldReturnNotFound() throws Exception {
        teamDto teamMock = new teamDto();
        teamMock.setId("teamId");
        ResponseEntity<teamDto> teamResponse = ResponseEntity.internalServerError().body(null);
        when(service.getTeam("teamLink")).thenReturn(teamResponse);
        this.mockMvc.perform(get("/team")
                        .param("teamLink", "teamLink"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchTeamResponseWhenAthleteReturnsErrorShouldReturnNotFound() throws Exception {
        teamDto teamMock = new teamDto();
        teamMock.setId("teamId");
        ResponseEntity<teamDto> teamResponse = ResponseEntity.ok(teamMock);
        List<athleteDto> athletes = new ArrayList<>();
        ResponseEntity<List<athleteDto>> athletesResponse = ResponseEntity.internalServerError().body(null);
        when(service.getTeam("teamLink")).thenReturn(teamResponse);
        this.mockMvc.perform(get("/team")
                        .param("teamLink", "teamLink"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchTeamResponseNoAthletesShouldReturnSuccess() throws Exception {
        teamDto teamMock = new teamDto();
        teamMock.setId("teamId");
        ResponseEntity<teamDto> teamResponse = ResponseEntity.ok(teamMock);
        List<athleteDto> athletes = new ArrayList<>();
        ResponseEntity<List<athleteDto>> athletesResponse = ResponseEntity.ok(athletes);

        when(service.getTeam("teamLink")).thenReturn(teamResponse);
        when(service.getActiveAthletesByTeamId("teamId", "teamLink")).thenReturn(athletesResponse);

        this.mockMvc.perform(get("/api/v1/team")
                        .param("teamLink", "teamLink"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"teamId\"")))
                .andExpect(jsonPath("$.athletes", org.hamcrest.Matchers.hasSize(0)));
    }



}