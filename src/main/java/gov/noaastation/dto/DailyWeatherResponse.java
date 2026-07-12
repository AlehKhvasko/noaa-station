package gov.noaastation.dto;

import java.time.LocalDate;

public record DailyWeatherResponse(
        LocalDate date,
        String stationId,
        Double precipitationInches,
        Double snowfallInches,
        Double snowDepthInches
) {
}