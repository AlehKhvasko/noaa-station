CREATE TABLE weather_data (
    id BIGSERIAL PRIMARY KEY,
    station_id VARCHAR(20) NOT NULL,
    observation_date DATE NOT NULL,
    element VARCHAR(10) NOT NULL,
    value INTEGER NOT NULL,
    m_flag VARCHAR(1),
    q_flag VARCHAR(1),
    s_flag VARCHAR(1),
    obs_time VARCHAR(4)
);