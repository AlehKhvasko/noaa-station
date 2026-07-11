package gov.noaastation.repository;

import gov.noaastation.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, String> {
    List<Station> findByStateCodeIgnoreCaseOrderByNameAsc(String stateCode);

    @Query("""
            SELECT DISTINCT s.stateCode
            FROM Station s
            WHERE s.stateCode IS NOT NULL
              AND s.stateCode <> ''
            ORDER BY s.stateCode
            """)
    List<String> findDistinctStateCodes();
}
