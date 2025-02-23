package ncaaprs.ncaaprs_service.service.dto;

import lombok.Data;
import ncaaprs.ncaaprs_service.constants.EventKeys;

import java.util.HashMap;
import java.util.Map;

@Data
public class ScrapedAthleteDto {
    private String name;
    private String link;
    private Map<EventKeys, String> currentPrs = new HashMap<>();



}
