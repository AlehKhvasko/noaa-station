package gov.noaastation.service;

import gov.noaastation.entity.Station;
import gov.noaastation.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public List<Station> getStationsByState(String state) {
        return stationRepository.findByStateCode(state.toUpperCase());
    }
}