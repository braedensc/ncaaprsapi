package ncaaprs.ncaaprs_service.controller.dto;

import lombok.Data;

@Data
public class eventDto {
    private String id;
    private String name; // Event name (e.g., "100m", "High Jump", etc.)
    private String metric; // Type of metric ("distance", "time", or "height")
    private String performanceFormat; // Format of performance (e.g., "time: mm:ss", "height: 2.08m")
}
