package ncaaprs.ncaaprs_service.repository.Interfaces;

import ncaaprs.ncaaprs_service.repository.models.Team;

import java.util.List;
import java.util.Optional;

public interface ITeamRepository {
    Team findById(String id);
    List<Team> findByName(String name);
    Optional<Team> findTeamIdByLink(String link);
    Team save(Team team);
    void incrementFailureCount(String teamId);
}

