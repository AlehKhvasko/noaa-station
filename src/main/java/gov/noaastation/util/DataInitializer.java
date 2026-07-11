package gov.noaastation.util;

import gov.noaastation.entity.WeatherData;
import gov.noaastation.repository.WeatherDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
public class DataInitializer {

    private static final int IMPORT_BATCH_SIZE = 5_000;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final WeatherDataRepository weatherDataRepository;
    private final ResourcePatternResolver resourcePatternResolver;


    public DataInitializer(
            WeatherDataRepository weatherDataRepository,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.weatherDataRepository = weatherDataRepository;
        this.resourcePatternResolver = resourcePatternResolver;
    }


    @PostConstruct
    public void init() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(
                    "classpath*:csv/weather/*.csv"
            );
            log.info("Reading {} weather CSV files... ", resources.length);

            for (Resource resource : resources) {
                importWeatherFile(resource);
            }

        } catch (Exception exception) {
            log.error("Error reading weather CSV files", exception);
        }
    }

    /**
     0 = station ID
     1 = date
     2 = element
     3 = value
     4 = measurement flag
     5 = quality flag
     6 = source flag
     7 = observation time
     **/
    private void importWeatherFile(Resource resource) throws IOException {
        log.info("Importing data from {}", resource.getFilename());

        List<WeatherData> batch = new ArrayList<>();

        try(Reader reader = new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );
            CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);
        ){
            long importedRecords = 0;
            //TODO optimize?
            for (CSVRecord record : csvParser) {
                WeatherData weatherData = WeatherData.builder()
                        .stationId(record.get(0).trim())
                        .date(LocalDate.parse(record.get(1).trim(), DATE_FORMATTER))
                        .element(record.get(2).trim())
                        .value(Integer.parseInt(record.get(3).trim()))
                        .mFlag(record.get(4).isBlank() ? null : record.get(4).trim())
                        .qFlag(record.get(5).isBlank() ? null : record.get(5).trim())
                        .sFlag(record.get(6).isBlank() ? null : record.get(6).trim())
                        .obsTime(record.get(7).isBlank() ? null : record.get(7).trim())
                        .build();

                batch.add(weatherData);

                if (batch.size() >= IMPORT_BATCH_SIZE) {
                    weatherDataRepository.saveAll(batch);
                    importedRecords += batch.size();

                    if (importedRecords % 100_000 == 0) {
                        log.info("Imported {} weather records", importedRecords);
                    }
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                weatherDataRepository.saveAll(batch);
            }
            log.info("Finished importing {}", resource.getFilename());

        }
    }
}