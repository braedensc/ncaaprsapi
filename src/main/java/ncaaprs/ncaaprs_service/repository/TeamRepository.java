package ncaaprs.ncaaprs_service.repository;

import ncaaprs.ncaaprs_service.repository.Interfaces.ITeamRepository;
import ncaaprs.ncaaprs_service.repository.models.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TeamRepository implements ITeamRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Team findById(String id) {
        return mongoTemplate.findById(id, Team.class);
    }

    @Override
    public List<Team> findByName(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return mongoTemplate.find(query, Team.class);
    }

    @Override
    public Optional<Team> findTeamIdByLink(String link) {
        String sanitizedLink = link.split("\\?")[0];
        Query query = new Query(Criteria.where("link").is(sanitizedLink));
        Team team = mongoTemplate.findOne(query, Team.class);
        return Optional.ofNullable(team);
    }


    @Override
    public Team save(Team team) {
        team.setLink(team.getLink().split("\\?")[0]);
        if (team.getId() == null) {
            team.setId(new org.bson.types.ObjectId().toHexString());
            return mongoTemplate.insert(team);
        }
            Team existingTeam = mongoTemplate.findById(team.getId(), Team.class);
            if (existingTeam != null) {
                // Check if the version matches
                if (!Objects.equals(existingTeam.getVersion(), team.getVersion())) {
                    throw new MongoTransactionException("Team version conflict occurred. Please try again.");
                }
            }
        return mongoTemplate.save(team);
    }


    public void incrementFailureCount(String teamId) {
        Query query = new Query(Criteria.where("id").is(teamId));
        Update update = new Update().inc("failureCount", 1)
                                    .set("failingSince", System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, Team.class);
    }
}
