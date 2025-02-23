package ncaaprs.ncaaprs_service.repository.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
//used within athlete class to track pr's, both current and historic. See Athlete.java comment for example
public class PerformanceRecord {

    private String eventId;   // Reference to the event (ID)
    private String currentPr; // Current Personal Record (PR) for this event
    private List<PrHistory> history = new ArrayList<>(); // History of past PRs for this event

}





