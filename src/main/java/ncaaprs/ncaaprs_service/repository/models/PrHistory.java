package ncaaprs.ncaaprs_service.repository.models;

import lombok.Data;

@Data
public class PrHistory {

    private long timestamp; // Unix timestamp of the PR update (when it was added to the db)
    private String pr; // The PR value (e.g., "2.08m" or "1:54.57")

    // Constructor
    public PrHistory(long timestamp, String pr) {
        this.timestamp = timestamp;
        this.pr = pr;
    }
}
