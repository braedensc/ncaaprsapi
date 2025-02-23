package ncaaprs.ncaaprs_service.repository;


import ncaaprs.ncaaprs_service.repository.Interfaces.IEventRepository;
import ncaaprs.ncaaprs_service.repository.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventRepository implements IEventRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Event findById(String id) {
        return mongoTemplate.findById(id, Event.class);
    }

    @Override
    public List<Event> findByName(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return mongoTemplate.find(query, Event.class);
    }

    @Override
    public Event save(Event event) {
        return mongoTemplate.save(event);
    }
}
