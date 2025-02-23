package ncaaprs.ncaaprs_service.controller.dto;

import lombok.Data;
import ncaaprs.ncaaprs_service.repository.models.PrHistory;

import java.util.List;


@Data
public class performanceRecordDto {

    private String eventId; 
    private String currentPr;
    private List<prHistoryDto> history;
}
