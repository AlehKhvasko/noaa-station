package gov.noaastation.controller;

import gov.noaastation.dto.DailyWeatherResponse;
import gov.noaastation.entity.Station;
import gov.noaastation.repository.StationRepository;
import gov.noaastation.service.WeatherRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class StationController {

    private final StationRepository stationRepository;
    private final WeatherRecordsService weatherRecordsService;

    public StationController(
            StationRepository stationRepository,
            WeatherRecordsService weatherRecordsService
    ) {
        this.stationRepository = stationRepository;
        this.weatherRecordsService = weatherRecordsService;
    }

    // Sample: /api/v1/states
    @GetMapping("/states")
    public List<String> getStates() {
        List<String> states =
                stationRepository.findDistinctStateCodes();

        log.info("Found {} distinct state codes", states.size());

        return states;
    }

    // Sample: /api/v1/stations?state=TX
    @GetMapping("/stations")
    public List<Station> getStationsByState(
            @RequestParam String state
    ) {
        List<Station> stations =
                stationRepository
                        .findByStateCodeIgnoreCaseOrderByNameAsc(state);

        log.info(
                "Found {} stations for state={}",
                stations.size(),
                state
        );

        return stations;
    }

    // Sample: /api/v1/stations/USW00012918
    @GetMapping("/stations/{stationId}")
    public ResponseEntity<Station> getStation(
            @PathVariable String stationId
    ) {
        return stationRepository.findById(stationId)
                .map(station -> {
                    log.info(
                            "Station found with stationId={}",
                            stationId
                    );

                    return ResponseEntity.ok(station);
                })
                .orElseGet(() -> {
                    log.warn(
                            "Station not found with stationId={}",
                            stationId
                    );

                    return ResponseEntity.notFound().build();
                });
    }

    // Sample: /api/v1/stations/USW00012918/weather?from=2024-01-01&to=2024-01-31
    @GetMapping(
            value = "/stations/{stationId}/weather",
            params = {"from", "to"}
    )
    public List<DailyWeatherResponse> getWeatherByStationAndDateRange(
            @PathVariable String stationId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        log.info(
                "Fetching daily weather for stationId={} from={} to={}",
                stationId,
                from,
                to
        );

        List<DailyWeatherResponse> weather =
                weatherRecordsService.findDailyWeather(
                        stationId,
                        from,
                        to
                );

        log.info(
                "Found {} daily weather records for stationId={} from={} to={}",
                weather.size(),
                stationId,
                from,
                to
        );

        return weather;
    }
}
