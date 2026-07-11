package gov.noaastation.repository;

import gov.noaastation.entity.Records;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherDataRepository extends JpaRepository<Records, Long> {
}
