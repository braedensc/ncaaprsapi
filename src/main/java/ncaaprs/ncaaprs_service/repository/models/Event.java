package ncaaprs.ncaaprs_service.repository.models;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "events")
@Data
//Events should be static in db, and only added, updated, or removed manually
public class Event {
    @Id
    private String id;
    private String name; // Event name (e.g., "100m", "High Jump", etc.)
    private String metric; // Type of metric ("distance", "time", or "height")
    private String performanceFormat; // Format of performance (e.g., "time: mm:ss", "height: 2.08m")
}
//Example Event
//{
//        "_id": "eventHJ",
//        "name": "High Jump",
//        "metric": "height",
//        "performanceFormat": "m.cm"
//        }


