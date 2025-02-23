package ncaaprs.ncaaprs_service.repository.Interfaces;

import ncaaprs.ncaaprs_service.constants.EventKeys;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.PrHistory;

import java.util.List;
import java.util.Map;

public interface IAthleteRepository {
    Athlete findById(String id);
    Athlete findByLink(String link);
    List<Athlete> findByName(String name);
    Athlete save(Athlete athlete);
    void saveAll(List<Athlete> athletes);
    void updatePrHistory(String athleteId, Map<EventKeys, String> currentPrs);

    List<String> getAthleteLinksByTeamId(String teamId);

    List<Athlete> getActiveAthletesByTeamId(String teamId);

    void markAthletesAsInactiveByLinks(List<String> athleteLinks);
}

