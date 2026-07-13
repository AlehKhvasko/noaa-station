package gov.noaastation.service;

import gov.noaastation.dto.DailyWeatherResponse;
import gov.noaastation.entity.Records;
import gov.noaastation.repository.WeatherDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherRecordsServiceTest {

    @Mock
    private WeatherDataRepository recordsRepository;

    @InjectMocks
    private WeatherRecordsService weatherRecordsService;

    @Test
    void findDailyWeatherGroupsRecordsByDateAndConvertsValuesToInches() {
        String stationId = "GHCND:USW00094846";
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 2);

        Records rain = Records.builder()
                .stationId(stationId)
                .date(from)
                .element("PRCP")
                .value(254)
                .build();
        Records snow = Records.builder()
                .stationId(stationId)
                .date(from)
                .element("SNOW")
                .value(51)
                .build();
        Records snowDepth = Records.builder()
                .stationId(stationId)
                .date(from)
                .element("SNWD")
                .value(127)
                .build();
        Records nextDayRain = Records.builder()
                .stationId(stationId)
                .date(to)
                .element("PRCP")
                .value(0)
                .build();

        when(recordsRepository.findByStationIdAndDateBetweenOrderByDateAsc(
                stationId,
                from,
                to
        )).thenReturn(List.of(rain, snow, snowDepth, nextDayRain));

        List<DailyWeatherResponse> responses =
                weatherRecordsService.findDailyWeather(stationId, from, to);

        assertEquals(2, responses.size());

        DailyWeatherResponse firstDay = responses.getFirst();
        assertEquals(from, firstDay.date());
        assertEquals(stationId, firstDay.stationId());
        assertEquals(1.0, firstDay.precipitationInches());
        assertEquals(2.01, firstDay.snowfallInches());
        assertEquals(5.0, firstDay.snowDepthInches());

        DailyWeatherResponse secondDay = responses.get(1);
        assertEquals(to, secondDay.date());
        assertEquals(stationId, secondDay.stationId());
        assertEquals(0.0, secondDay.precipitationInches());
        assertNull(secondDay.snowfallInches());
        assertNull(secondDay.snowDepthInches());

        verify(recordsRepository)
                .findByStationIdAndDateBetweenOrderByDateAsc(stationId, from, to);
    }

    @Test
    void findDailyWeatherSkipsRecordsWithoutValues() {
        String stationId = "GHCND:USW00094846";
        LocalDate date = LocalDate.of(2024, 1, 1);

        Records missingValue = Records.builder()
                .stationId(stationId)
                .date(date)
                .element("PRCP")
                .value(null)
                .build();

        when(recordsRepository.findByStationIdAndDateBetweenOrderByDateAsc(
                stationId,
                date,
                date
        )).thenReturn(List.of(missingValue));

        List<DailyWeatherResponse> responses =
                weatherRecordsService.findDailyWeather(stationId, date, date);

        assertTrue(responses.isEmpty());
    }
}
