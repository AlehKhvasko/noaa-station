package gov.noaastation.service;

import gov.noaastation.dto.DailyWeatherResponse;
import gov.noaastation.entity.Records;
import gov.noaastation.repository.WeatherDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherRecordsService {

    private final WeatherDataRepository weatherDataRepository;

    public WeatherRecordsService(
            WeatherDataRepository weatherDataRepository
    ) {
        this.weatherDataRepository = weatherDataRepository;
    }

    // Returns one mapped weather response per day for a station.
    public List<DailyWeatherResponse> findDailyWeather(
            String stationId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Records> records = weatherDataRepository
                .findByStationIdAndDateBetweenOrderByDateAsc(
                        stationId,
                        startDate,
                        endDate
                );

        Map<LocalDate, DailyWeatherResponse> weather = new HashMap<>();

        for (Records record : records) {
            if (record.getValue() == null) {
                continue;
            }

            DailyWeatherResponse day = weather.get(record.getDate());

            if (day == null) {
                day = new DailyWeatherResponse(
                        record.getDate(),
                        record.getStationId(),
                        null,
                        null,
                        null
                );
            }

            // PRCP = precipitation, SNOW = snowfall, SNWD = snow depth.
            switch (record.getElement()) {
                case "PRCP" ->
                        day = new DailyWeatherResponse(
                                day.date(),
                                day.stationId(),
                                precipitationToInches(record.getValue()),
                                day.snowfallInches(),
                                day.snowDepthInches()
                        );

                case "SNOW" ->
                        day = new DailyWeatherResponse(
                                day.date(),
                                day.stationId(),
                                day.precipitationInches(),
                                millimetersToInches(record.getValue()),
                                day.snowDepthInches()
                        );

                case "SNWD" ->
                        day = new DailyWeatherResponse(
                                day.date(),
                                day.stationId(),
                                day.precipitationInches(),
                                day.snowfallInches(),
                                millimetersToInches(record.getValue())
                        );

                default -> {
                    // Ignore unsupported elements
                }
            }

            weather.put(record.getDate(), day);
        }

        return weather.values()
                .stream()
                .sorted(Comparator.comparing(DailyWeatherResponse::date))
                .toList();
    }

    //tenths of millimeters to inches.
    private double precipitationToInches(int rawValue) {
        double millimeters = rawValue / 10.0;
        return round(millimeters / 25.4);
    }

    private double millimetersToInches(int rawValue) {
        return round(rawValue / 25.4);
    }

    // two decimal places
    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.UP)
                .doubleValue();
    }
}
