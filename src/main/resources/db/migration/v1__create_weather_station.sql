CREATE TABLE station (
     station_id VARCHAR(11) PRIMARY KEY,
     latitude DOUBLE PRECISION NOT NULL,
     longitude DOUBLE PRECISION NOT NULL,
     elevation_meters DOUBLE PRECISION,
     state_code VARCHAR(2),
     name VARCHAR(100) NOT NULL,
     gsn_flag VARCHAR(3),
     hcn_crn_flag VARCHAR(3),
     wmo_id VARCHAR(5)
);