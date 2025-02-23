package ncaaprs.ncaaprs_service.service.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import ncaaprs.ncaaprs_service.constants.EventKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
public class ScraperResponseDto {
    private List<ScrapedAthleteDto> parsedAthletes = new ArrayList<>();
    private ScrapedTeamDto team = new ScrapedTeamDto();



    @JsonAnySetter
    public void parseAthletesFromScraperResponse(String key, Object value) {
        if ("athletes".equals(key)) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) value;
            boolean isTeamDataSet = false;
            for (Map<String, Object> record : records) {
                ScrapedAthleteDto athlete = new ScrapedAthleteDto();
                athlete.setName((String) record.get("name"));
                athlete.setLink((String) record.get("link"));
                if (!isTeamDataSet) {
                    this.team.setTeamType((String) record.get("teamType"));
                    this.team.setLogo((String) record.get("logo"));
                    this.team.setTitle((String) record.get("title"));
                    isTeamDataSet = true;
                }

               for (EventKeys eventKey : EventKeys.values()) {
                    String eventKeyString = eventKey.name();
                    if (record.containsKey(eventKeyString) && record.get(eventKeyString) != null) {
                        athlete.getCurrentPrs().put(eventKey, record.get(eventKeyString).toString());
                    }
                }
                    this.parsedAthletes.add(athlete);
                }
            }
        }
    }
