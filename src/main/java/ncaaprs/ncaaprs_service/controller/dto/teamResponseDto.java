package ncaaprs.ncaaprs_service.controller.dto;


import lombok.Data;

import java.util.List;

@Data
public class teamResponseDto {
    private teamDto team;
    private List<athleteDto> athletes;
}
