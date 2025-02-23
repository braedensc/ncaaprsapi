package ncaaprs.ncaaprs_service.repository.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "teams")
@Data
public class Team {
    @Id
    private String id;
    @Version
    private Long version = 0L;
    @Indexed(unique = true)
    private String link;


    private String teamName;
    private String teamType;
    private String logo;

    private long lastUpdatedOn; // Unix timestamp of the last update
    private int failureCount = 0;   // Number of consecutive failures for the scraper
    private Long failingSince;  // Unix timestamp of when failures started (nullable)
}


//Example Team
//{
//        "_id": "team123",
//        "teamName": "Georgia Tech",
//        "teamType": "MEN'S TRACK & FIELD",
//        "logo": "https://logos.tfrrs.org/georgia-tech.png",
//        "athleteIds": [
//        "athlete123",
//        "athlete456",
//        "athlete789"
//        ],
//        "lastUpdatedOn": 1702387200,
//        "failureCount": 2,
//        "failingSince": 1702300800
//        }


