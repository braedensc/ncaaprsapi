package ncaaprs.ncaaprs_service.service.mapper;

import ncaaprs.ncaaprs_service.constants.EventKeys;
import ncaaprs.ncaaprs_service.controller.dto.athleteDto;
import ncaaprs.ncaaprs_service.controller.dto.performanceRecordDto;
import ncaaprs.ncaaprs_service.repository.models.Athlete;


import ncaaprs.ncaaprs_service.repository.models.PerformanceRecord;
import ncaaprs.ncaaprs_service.service.dto.ScrapedAthleteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mapper(componentModel = "spring")
public interface AthleteMapper {
        @Mapping(target = "teamId", source = "teamId")
        @Mapping(target = "teamLogo", source = "teamLogo")
        @Mapping(target = "teamTitle", source = "teamTitle")
        @Mapping(target = "lastUpdatedOn", expression = "java(System.currentTimeMillis())")
        @Mapping(target = "active", constant = "true")
        @Mapping(target = "prs", source = "scrapedAthleteDto.currentPrs", qualifiedByName = "mapToPerformanceRecords")
        Athlete toEntity(ScrapedAthleteDto scrapedAthleteDto, String teamId, String teamLogo, String teamTitle);


        athleteDto toDto(Athlete athlete);

    @Named("mapToPerformanceRecords")
    public default List<PerformanceRecord> mapToPerformanceRecords(Map<EventKeys, String> currentPrs) {
        if (currentPrs == null) return null;
        List<PerformanceRecord> performanceRecords = new ArrayList<>();
        currentPrs.forEach((key, value) -> {
            PerformanceRecord record = new PerformanceRecord();
            record.setEventId(key.name());
            record.setCurrentPr(value);
            performanceRecords.add(record);
        });
        return performanceRecords;
    }


}
