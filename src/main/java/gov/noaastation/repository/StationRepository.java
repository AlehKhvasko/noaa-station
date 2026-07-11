package gov.noaastation.repository;

import gov.noaastation.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, String> {
    List<Station> findByStateCode(String stateCode);

}
