package ncaaprs.ncaaprs_service.service.mapper;

import ncaaprs.ncaaprs_service.controller.dto.teamDto;
import ncaaprs.ncaaprs_service.repository.models.Team;
import ncaaprs.ncaaprs_service.service.dto.ScrapedTeamDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target="teamName", source = "scrapedTeamDto.title")
    @Mapping(target = "link", source = "teamLink")
    @Mapping(target="lastUpdatedOn", expression = "java(System.currentTimeMillis())")
    Team toEntity(ScrapedTeamDto scrapedTeamDto, String teamLink);


    teamDto toDto(Team team);



}
