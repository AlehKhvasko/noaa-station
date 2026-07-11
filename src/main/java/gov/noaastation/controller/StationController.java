package gov.noaastation.controller;

import gov.noaastation.entity.Station;
import gov.noaastation.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService weatherStationService;

    @GetMapping
    public List<Station> getStationsByState(@RequestParam String state) {
        return weatherStationService.getStationsByState(state);
    }
}