package gov.noaastation.util;

import gov.noaastation.entity.Station;
import gov.noaastation.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WeatherStationInitializer {

    private static final int IMPORT_BATCH_SIZE = 10_000;

    private final StationRepository stationRepository;
    private final ResourcePatternResolver resourcePatternResolver;

    public WeatherStationInitializer(
            StationRepository stationRepository,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.stationRepository = stationRepository;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @PostConstruct
    public void init() {
        /*
          Temp flag to avoid overflowing DB
         */
//        if (stationRepository.count() > 0) {
//            log.info("Station data already exists. Skipping station CSV import.");
//            return;
//        }

        try {
            Resource[] resources = resourcePatternResolver.getResources(
                    "classpath*:csv/station/*.csv"
            );

            log.info("Reading {} station CSV files...", resources.length);

            for (Resource resource : resources) {
                importStationFile(resource);
            }

        } catch (Exception exception) {
            log.error("Error reading station CSV files", exception);
        }
    }

    /**
     * 0 = station ID
     * 1 = latitude
     * 2 = longitude
     * 3 = elevation
     * 4 = state code
     * 5 = station name
     * 6 = GSN flag
     * 7 = HCN/CRN flag
     * 8 = WMO ID
     */
    private void importStationFile(Resource resource) throws IOException {
        log.info("Importing station data from {}", resource.getFilename());

        List<Station> batch = new ArrayList<>(IMPORT_BATCH_SIZE);
        long importedStations = 0;

        try (
                Reader reader = new InputStreamReader(
                        resource.getInputStream(),
                        StandardCharsets.UTF_8
                );
                CSVParser csvParser = CSVFormat.DEFAULT.builder()
                        .setQuote(null)
                        .get()
                        .parse(reader)

        ) {
            for (CSVRecord record : csvParser) {
                String stationId = record.get(0).trim();

                Station station = Station.builder()
                        .stationId(stationId)
                        .countryCode(stationId.substring(0, 2))
                        .latitude(Double.parseDouble(record.get(1).trim()))
                        .longitude(Double.parseDouble(record.get(2).trim()))
                        .elevationMeters(parseNullableDouble(record.get(3)))
                        .stateCode(emptyToNull(record.get(4)))
                        .name(record.get(5).trim())
                        .build();

                batch.add(station);

                if (batch.size() >= IMPORT_BATCH_SIZE) {
                    long start = System.nanoTime();

                    stationRepository.saveAll(batch);
                    stationRepository.flush();

                    long elapsed = System.nanoTime() - start;

                    importedStations += batch.size();

                    log.info(
                            "Inserted {} stations in {} seconds. Total imported: {}",
                            batch.size(),
                            String.format("%.3f", elapsed / 1_000_000_000.0),
                            importedStations
                    );

                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                stationRepository.saveAll(batch);
                stationRepository.flush();

                importedStations += batch.size();
                batch.clear();
            }
        }

        log.info(
                "Finished importing {}. Imported {} stations.",
                resource.getFilename(),
                importedStations
        );
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private Double parseNullableDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Double.parseDouble(value.trim());
    }
}