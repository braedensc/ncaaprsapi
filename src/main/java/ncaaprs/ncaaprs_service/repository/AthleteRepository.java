package ncaaprs.ncaaprs_service.repository;

import ncaaprs.ncaaprs_service.constants.EventKeys;
import ncaaprs.ncaaprs_service.repository.Interfaces.IAthleteRepository;
import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.PrHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
    public class AthleteRepository implements IAthleteRepository {

        @Autowired
        private MongoTemplate mongoTemplate;

        @Override
        public Athlete findById(String id) {
            return mongoTemplate.findById(id, Athlete.class);
        }

    @Override
    public Athlete findByLink(String link) {
        String sanitizedLink = sanitizeLink(link);
        Query query = new Query(Criteria.where("link").is(sanitizedLink));
        return mongoTemplate.findOne(query, Athlete.class);
    }

        @Override
        public List<Athlete> findByName(String name) {
            Query query = new Query(Criteria.where("name").is(name));
            return mongoTemplate.find(query, Athlete.class);
        }

        @Override
        public Athlete save(Athlete athlete) {
            return mongoTemplate.save(athlete);
        }

    @Override
    public void saveAll(List<Athlete> athletes) {
        if (athletes == null || athletes.isEmpty()) {
            return;
        }
        for (Athlete athlete : athletes) {
            athlete.setLink(sanitizeLink(athlete.getLink()));
            boolean exists = mongoTemplate.exists(
                    Query.query(Criteria.where("_id").is(athlete.getId())), Athlete.class);

            mongoTemplate.save(athlete);

            if (exists) {
               // log.info("✅ Updated athlete: " + athlete.getName());
            } else {
               // log.info("🆕 Inserted new athlete: " + athlete.getName());
            }
        }
    }


    @Override
        public void updatePrHistory(String athleteId, Map<EventKeys, String> currentPrs) {
            for (Map.Entry<EventKeys, String> entry : currentPrs.entrySet()) {
                String eventId = entry.getKey().name();
                String currentPr = entry.getValue();
                PrHistory newHistory = new PrHistory(System.currentTimeMillis(), currentPr);
                Query query = new Query(Criteria.where("id").is(athleteId)
                        .and("prs.eventId").is(eventId));
                Update update = new Update().push("prs.$.history", newHistory);
                mongoTemplate.updateFirst(query, update, Athlete.class);
            }
        }

        public List<String> getAthleteLinksByTeamId(String teamId) {
            Query query = new Query(Criteria.where("teamId").is(teamId));
            query.fields().include("link");
            List<Athlete> athletes = mongoTemplate.find(query, Athlete.class);
            List<String> athleteLinks = new ArrayList<>();
            for (Athlete athlete : athletes) {
                athleteLinks.add(athlete.getLink());
            }
            return athleteLinks;
        }

    public List<Athlete> getActiveAthletesByTeamId(String teamId) {
        Query query = new Query(Criteria.where("teamId").is(teamId).and("active").is(true));
        return mongoTemplate.find(query, Athlete.class);
    }

    public void markAthletesAsInactiveByLinks(List<String> athleteLinks) {
        if (athleteLinks == null || athleteLinks.isEmpty()) {
            return;
        }
        List<String> sanitizedLinks = athleteLinks.stream()
                .map(this::sanitizeLink)
                .toList();
        Query query = new Query(Criteria.where("link").in(sanitizedLinks));
        Update update = new Update()
                .set("active", false)
                .set("lastUpdatedOn", System.currentTimeMillis());
        mongoTemplate.updateMulti(query, update, Athlete.class);
    }
    private String sanitizeLink(String link) {
        if (link == null) {
            return null;
        }
        return link.split("\\?")[0];  // ✅ Remove query parameters
    }




}
