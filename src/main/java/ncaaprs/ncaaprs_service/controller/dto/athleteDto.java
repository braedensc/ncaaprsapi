package ncaaprs.ncaaprs_service.controller.dto;

import lombok.Data;
import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;
import java.util.List;



@Data
public class athleteDto {
    private String id;

    private String name;
    private String link;
    private String teamId;
    private List<performanceRecordDto> prs; // List of performance records (current PRs and history)
    private long lastUpdatedOn;  // Unix timestamp for last update
    private String teamLogo;
    private String teamTitle;
    private boolean active;
}
