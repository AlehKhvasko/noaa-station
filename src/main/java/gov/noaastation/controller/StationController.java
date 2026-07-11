package gov.noaastation.controller;

import gov.noaastation.entity.Records;
import gov.noaastation.entity.Station;
import gov.noaastation.repository.StationRepository;
import gov.noaastation.repository.WeatherDataRepository;
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
    private final WeatherDataRepository weatherDataRepository;

    public StationController(
            StationRepository stationRepository,
            WeatherDataRepository weatherDataRepository
    ) {
        this.stationRepository = stationRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    /*
     * Used to populate the state dropdown.
     * Sample: /api/v1/states
     */
    @GetMapping("/states")
    public List<String> getStates() {
        List<String> states = stationRepository.findDistinctStateCodes();

        log.info("Found {} distinct state codes", states.size());
        return states;
    }

    /*
     * Used after the user selects a state.
     * Sample: /api/v1/stations?state=TX
     */
    @GetMapping("/stations")
    public List<Station> getStationsByState(
            @RequestParam String state
    ) {

        List<Station> stations =
                stationRepository.findByStateCodeIgnoreCaseOrderByNameAsc(state);

        log.info(
                "Found {} stations for state={}",
                stations.size(),
                state
        );

        return stations;
    }

    /*
     * Used to retrieve one station.
     * Sample: /api/v1/stations/US1TXCLL005
     */
    @GetMapping("/stations/{stationId}")
    public ResponseEntity<Station> getStation(
            @PathVariable String stationId
    ) {

        return stationRepository.findById(stationId)
                .map(station -> {
                    log.info("Station found with stationId={}", stationId);
                    return ResponseEntity.ok(station);
                })
                .orElseGet(() -> {
                    log.warn("Station not found with stationId={}", stationId);
                    return ResponseEntity.notFound().build();
                });
    }

    /*
     * Sample: /api/v1/stations/US1TXCLL005/weather
     */
    @GetMapping(
            value = "/stations/{stationId}/weather",
            params = {"!from", "!to"}
    )
    public List<Records> getWeatherByStation(
            @PathVariable String stationId
    ) {
        List<Records> records =
                weatherDataRepository.findByStationIdOrderByDateDesc(stationId);

        log.info(
                "Found {} weather records for stationId={}",
                records.size(),
                stationId
        );

        return records;
    }

    /*
     * Sample:
     * /api/v1/stations/US1TXCLL005/weather?from=2026-01-01&to=2026-07-11
     */
    @GetMapping(
            value = "/stations/{stationId}/weather",
            params = {"from", "to"}
    )
    public List<Records> getWeatherByStationAndDateRange(
            @PathVariable String stationId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        log.info(
                "Fetching weather records for stationId={} from={} to={}",
                stationId,
                from,
                to
        );

        List<Records> records =
                weatherDataRepository
                        .findByStationIdAndDateBetweenOrderByDateAsc(
                                stationId,
                                from,
                                to
                        );

        log.info(
                "Found {} weather records for stationId={} from={} to={}",
                records.size(),
                stationId,
                from,
                to
        );

        return records;
    }
}