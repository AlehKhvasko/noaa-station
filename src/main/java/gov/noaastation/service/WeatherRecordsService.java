package gov.noaastation.service;

import gov.noaastation.dto.DailyWeatherResponse;
import gov.noaastation.entity.Records;
import gov.noaastation.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherRecordsService {

    private final WeatherDataRepository recordsRepository;

    public WeatherRecordsService(
            WeatherDataRepository recordsRepository
    ) {
        this.recordsRepository = recordsRepository;
    }

    public List<DailyWeatherResponse> findDailyWeather(
            String stationId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Records> records =
                recordsRepository
                        .findByStationIdAndDateBetweenOrderByDateAsc(
                                stationId,
                                startDate,
                                endDate
                        );

        Map<LocalDate, DailyWeatherBuilder> grouped =
                new LinkedHashMap<>();

        for (Records record : records) {
            if (record.getValue() == null) {
                continue;
            }

            DailyWeatherBuilder day = grouped.computeIfAbsent(
                    record.getDate(),
                    date -> new DailyWeatherBuilder(
                            date,
                            record.getStationId()
                    )
            );

            switch (record.getElement()) {
                case "PRCP" ->
                        day.precipitationInches =
                                precipitationToInches(record.getValue());

                case "SNOW" ->
                        day.snowfallInches =
                                millimetersToInches(record.getValue());

                case "SNWD" ->
                        day.snowDepthInches =
                                millimetersToInches(record.getValue());

                default -> {
                    // Ignore unsupported NOAA elements
                }
            }
        }

        return grouped.values()
                .stream()
                .map(DailyWeatherBuilder::build)
                .toList();
    }

    private double precipitationToInches(int rawValue) {
        double millimeters = rawValue / 10.0;
        return round(millimeters / 25.4);
    }

    private double millimetersToInches(int rawValue) {
        return round(rawValue / 25.4);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static class DailyWeatherBuilder {

        private final LocalDate date;
        private final String stationId;

        private Double precipitationInches;
        private Double snowfallInches;
        private Double snowDepthInches;

        private DailyWeatherBuilder(
                LocalDate date,
                String stationId
        ) {
            this.date = date;
            this.stationId = stationId;
        }

        private DailyWeatherResponse build() {
            return new DailyWeatherResponse(
                    date,
                    stationId,
                    precipitationInches,
                    snowfallInches,
                    snowDepthInches
            );
        }
    }
}