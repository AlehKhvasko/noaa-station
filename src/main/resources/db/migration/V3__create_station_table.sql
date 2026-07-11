CREATE TABLE station (
     station_id VARCHAR(20) PRIMARY KEY,
     country_code VARCHAR(2) NOT NULL,
     state_code VARCHAR(10),
     name VARCHAR(100) NOT NULL,
     latitude DOUBLE PRECISION NOT NULL,
     longitude DOUBLE PRECISION NOT NULL,
     elevation_meters DOUBLE PRECISION,
     gsn_flag VARCHAR(3),
     hcn_crn_flag VARCHAR(3),
     wmo_id VARCHAR(10)
);