package ncaaprs.ncaaprs_service.controller.dto;

import lombok.Data;
import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.util.List;



@Data
public class teamDto {
    private String id;
    private String teamName;
    private String teamType;
    private String logo;
    private String link;
    private long lastUpdatedOn;
    private int failureCount;
}
