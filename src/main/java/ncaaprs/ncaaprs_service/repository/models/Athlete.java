package ncaaprs.ncaaprs_service.repository.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "athletes")
@Data
public class Athlete {

    @Id
    private String id;

    @Indexed(unique = true)
    private String teamId;

    private String name;
    private String link;
    private List<PerformanceRecord> prs = new ArrayList<>();// List of performance records (current PRs and history)
    private long lastUpdatedOn;  // Unix timestamp for last update
    private String teamLogo;
    private String teamTitle;
    private boolean active;

}


//Example Athlete
//{
//        "_id": "athlete123",
//        "name": "Omar Arnaout",
//        "link": "//tfrrs.org/athletes/8323970/Georgia_Tech/Omar_Arnaout.html",
//        "teamId": "team123",
//        "prs": [
//        {
//        "eventId": "event123",
//        "currentPr": "2.10m 6 10",
//        "history": [
//        { "timestamp": 1702293600, "pr": "2.08m 6 9.75" },
//        { "timestamp": 1699737600, "pr": "2.05m 6 8.75" }
//        ]
//        },
//        {
//        "eventId": "event456",
//        "currentPr": "1:54.00",
//        "history": [
//        { "timestamp": 1702293600, "pr": "1:54.57" },
//        { "timestamp": 1699737600, "pr": "1:56.00" }
//        ]
//        }
//        ],
//        "lastUpdatedOn": 1702387200,
//        "logo": "https://logos.tfrrs.org/georgia-tech.png",
//        "title": "Georgia Tech"
//        }



