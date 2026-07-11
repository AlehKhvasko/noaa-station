package gov.noaastation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "station")
public class Station {

    @Id
    @Column(name = "station_id", length = 20)
    private String stationId;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "state_code", length = 10)
    private String stateCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "elevation_meters")
    private Double elevationMeters;

    @Column(name = "gsn_flag", length = 3)
    private String gsnFlag;

    @Column(name = "hcn_crn_flag", length = 3)
    private String hcnCrnFlag;

    @Column(name = "wmo_id", length = 10)
    private String wmoId;
}
