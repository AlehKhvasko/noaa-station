import { useEffect, useState } from "react";

type Station = {
  stationId: string;
  name: string;
  stateCode: string;
};

type WeatherRecord = {
  id: number;
  stationId: string;
  date: string;
  element: string;
  value: number;
};

function App() {
  const [states, setStates] = useState<string[]>([]);
  const [stations, setStations] = useState<Station[]>([]);
  const [records, setRecords] = useState<WeatherRecord[]>([]);

  const [state, setState] = useState("");
  const [stationId, setStationId] = useState("");
  const [from, setFrom] = useState("2026-01-01");
  const [to, setTo] = useState("2026-12-31");
  const [error, setError] = useState("");

  useEffect(() => {
    console.info("Loading states");

    fetch("http://localhost:8080/api/v1/states")
        .then((response) => {
          console.info("GET /api/v1/states", {
            status: response.status,
          });

          if (!response.ok) {
            throw new Error(`Failed to load states: ${response.status}`);
          }

          return response.json();
        })
        .then((data: string[]) => {
          console.info("States loaded", {
            count: data.length,
            states: data,
          });

          setStates(data);
        })
        .catch((error: unknown) => {
          console.error("Failed to load states", error);
          setError("Could not load states.");
        });
  }, []);

  function loadStations(selectedState: string) {
    console.info("State selected", {
      state: selectedState,
    });

    setState(selectedState);
    setStationId("");
    setRecords([]);
    setError("");

    if (!selectedState) {
      console.info("State cleared");
      setStations([]);
      return;
    }

    const url =
        `http://localhost:8080/api/v1/stations` +
        `?state=${(selectedState)}`;

    console.info("Loading stations", {
      state: selectedState,
      url,
    });

    fetch(url)
        .then((response) => {
          console.info("GET /api/v1/stations", {
            status: response.status,
            state: selectedState,
          });

          if (!response.ok) {
            throw new Error(`Failed to load stations: ${response.status}`);
          }

          return response.json();
        })
        .then((data: Station[]) => {
          console.info("Stations loaded", {
            state: selectedState,
            count: data.length,
          });

          setStations(data);
        })
        .catch((error: unknown) => {
          console.error("Failed to load stations", {
            state: selectedState,
            error,
          });

          setStations([]);
          setError("Could not load stations.");
        });
  }

  function loadWeather() {
    setError("");

    if (!stationId) {
      console.warn("No station selected");
      setError("Select a station first.");
      return;
    }

    if (from > to) {
      console.warn("Invalid date range", {
        from,
        to,
      });

      setError("From date must be before the To date.");
      return;
    }

    const params = new URLSearchParams({
      from,
      to,
    });

    const url =
        `http://localhost:8080/api/v1/stations/` +
        `${encodeURIComponent(stationId)}/weather?${params}`;

    console.info("Loading weather records", {
      stationId,
      from,
      to,
      url,
    });

    fetch(url)
        .then((response) => {
          console.info(" GET /api/v1/weather", {
            status: response.status,
            stationId,
            from,
            to,
          });

          if (!response.ok) {
            throw new Error(
                `Failed to load weather records: ${response.status}`
            );
          }

          return response.json();
        })
        .then((data: WeatherRecord[]) => {
          console.info("Weather records loaded", {
            stationId,
            count: data.length,
          });

          setRecords(data);
        })
        .catch((error: unknown) => {
          console.error("Failed to load weather records", {
            stationId,
            from,
            to,
            error,
          });

          setRecords([]);
          setError("Could not load weather records.");
        });
  }

  return (
      <main
          style={{
            maxWidth: "900px",
            margin: "40px auto",
            padding: "20px",
          }}
      >
        <h1>NOAA Weather</h1>

        <div
            style={{
              display: "flex",
              gap: "12px",
              flexWrap: "wrap",
            }}
        >
          <select
              value={state}
              onChange={(event) => loadStations(event.target.value)}
          >
            <option value="">Select state</option>

            {states.map((stateCode) => (
                <option key={stateCode} value={stateCode}>
                  {stateCode}
                </option>
            ))}
          </select>

          <select
              value={stationId}
              onChange={(event) => {
                const selectedId = event.target.value;

                console.info("Station selected", {
                  stationId: selectedId,
                });

                setStationId(selectedId);
              }}
              disabled={!state}
          >
            <option value="">Select station</option>

            {stations.map((station) => (
                <option
                    key={station.stationId}
                    value={station.stationId}
                >
                  {station.name} ({station.stationId})
                </option>
            ))}
          </select>

          <input
              type="date"
              value={from}
              onChange={(event) => {
                console.info("From date changed", {
                  from: event.target.value,
                });

                setFrom(event.target.value);
              }}
          />

          <input
              type="date"
              value={to}
              onChange={(event) => {
                console.info("To date changed", {
                  to: event.target.value,
                });

                setTo(event.target.value);
              }}
          />

          <button onClick={loadWeather}>Search</button>
        </div>

        {error && (
            <p style={{ color: "dark" }}>
              {error}
            </p>
        )}

        <table
            style={{
              width: "100%",
              marginTop: "30px",
              borderCollapse: "collapse",
            }}
        >
          <thead>
          <tr>
            <th style={cellStyle}>Date</th>
            <th style={cellStyle}>Station</th>
            <th style={cellStyle}>Element</th>
            <th style={cellStyle}>Value</th>
          </tr>
          </thead>

          <tbody>
          {records.map((record) => (
              <tr key={record.id}>
                <td style={cellStyle}>{record.date}</td>
                <td style={cellStyle}>{record.stationId}</td>
                <td style={cellStyle}>{record.element}</td>
                <td style={cellStyle}>{record.value}</td>
              </tr>
          ))}
          </tbody>
        </table>

        {records.length === 0 && (
            <p style={{ marginTop: "30px" }}>
              No weather records loaded.
            </p>
        )}
      </main>
  );
}

const cellStyle = {
  border: "1px solid #ccc",
  padding: "10px",
  textAlign: "left" as const,
};

export default App;