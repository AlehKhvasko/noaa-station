package gov.noaastation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;



@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "weather_data")

public class Records {
    @Id
    @SequenceGenerator(
            name = "weather_data_sequence",
            sequenceName = "weather_data_id_seq",
            allocationSize = 50
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "weather_data_sequence"
    )
    private Long id;

    //TODO String???
    @Column(name = "station_id", nullable = false)
    private String stationId;

    @Column(name = "observation_date", nullable = false)
    private LocalDate date;

    private String element;

    private Integer value;

    private String mFlag;

    private String qFlag;

    private String sFlag;

    private String obsTime;
}
