package ncaaprs.ncaaprs_service.repository.Interfaces;

import ncaaprs.ncaaprs_service.repository.models.Athlete;
import ncaaprs.ncaaprs_service.repository.models.Event;
import ncaaprs.ncaaprs_service.repository.models.Team;

import java.util.List;

public interface IEventRepository {
     Event findById(String id);
     List<Event> findByName(String name);
     Event save(Event event);

}

