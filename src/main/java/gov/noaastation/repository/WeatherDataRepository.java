package gov.noaastation.repository;

import gov.noaastation.entity.Records;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeatherDataRepository extends JpaRepository<Records, Long> {

    List<Records> findByStationIdOrderByDateDesc(
            String stationId
    );

    List<Records> findByStationIdAndDateBetweenOrderByDateAsc(
            String stationId,
            LocalDate from,
            LocalDate to
    );
}
