import { useEffect, useState } from "react";

type Station = {
    stationId: string;
    name: string;
    stateCode: string;
};

type DailyWeather = {
    date: string;
    stationId: string;
    precipitationInches: number | null;
    snowfallInches: number | null;
    snowDepthInches: number | null;
};

function App() {
    const [states, setStates] = useState<string[]>([]);
    const [stations, setStations] = useState<Station[]>([]);
    const [records, setRecords] = useState<DailyWeather[]>([]);

    const [state, setState] = useState("");
    const [stationId, setStationId] = useState("");
    const [from, setFrom] = useState("2026-01-01");
    const [to, setTo] = useState("2026-12-31");
    const [error, setError] = useState("");

    const MainURL = "http://localhost:8080/api/v1/";

    useEffect(() => {
        fetch(MainURL + "states")
            .then((response) => {
                console.log("States response status:", response.status);

                if (!response.ok) {
                    throw new Error(
                        `Failed to load states: ${response.status}`
                    );
                }

                return response.json();
            })
            .then((data: string[]) => {
                console.log("States data:", data);
                console.log("States count:", data.length);

                setStates(data);
            })
            .catch((error: unknown) => {
                console.error("Failed to load states:", error);
                setError("Could not load states.");
            });
    }, []);

    async function loadStations(selectedState: string) {
        console.log("Selected state:", selectedState);

        setState(selectedState);
        setStationId("");
        setRecords([]);
        setError("");

        if (!selectedState) {
            setStations([]);
            return;
        }

        const url = MainURL + `stations?state=${selectedState}`;

        console.log("Stations request URL:", url);

        try {
            const response = await fetch(url);

            console.log("Stations response status:", response.status);

            if (!response.ok) {
                throw new Error(
                    `Failed to load stations: ${response.status}`
                );
            }

            const data: Station[] = await response.json();

            console.log("Stations data:", data);
            console.log("Stations count:", data.length);

            if (data.length === 0) {
                console.warn(
                    `No stations returned for state ${selectedState}`
                );
            }

            setStations(data);
        } catch (error: unknown) {
            console.error("Failed to load stations:", error);
            setStations([]);
            setError("Could not load stations.");
        }
    }

    async function loadWeather() {
        setError("");

        console.log("Weather search:", {
            state,
            stationId,
            from,
            to,
        });

        if (!stationId) {
            setError("Select a station first.");
            return;
        }

        if (from > to) {
            setError("From date must be before the To date.");
            return;
        }

        const url =
            MainURL +
            `stations/${stationId}/weather` +
            `?from=${from}` +
            `&to=${to}`;

        console.log("Weather request URL:", url);

        try {
            const response = await fetch(url);

            console.log("Weather response status:", response.status);

            if (!response.ok) {
                throw new Error(
                    `Failed to load weather records: ${response.status}`
                );
            }

            const data: DailyWeather[] = await response.json();

            console.log("Weather data:", data);
            console.log("Weather records count:", data.length);

            if (data.length === 0) {
                console.warn("Backend returned 0 weather records.", {
                    stationId,
                    from,
                    to,
                });
            }

            setRecords(data);
        } catch (error: unknown) {
            console.error("Failed to load weather records:", error);
            setRecords([]);
            setError("Could not load weather records.");
        }
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
                    onChange={(event) =>
                        loadStations(event.target.value)
                    }
                >
                    <option value="">Select state</option>

                    {states.map((stateCode) => (
                        <option
                            key={stateCode}
                            value={stateCode}
                        >
                            {stateCode}
                        </option>
                    ))}
                </select>

                <select
                    value={stationId}
                    onChange={(event) => {
                        const selectedStationId = event.target.value;

                        console.log(
                            "Selected station:",
                            selectedStationId
                        );

                        setStationId(selectedStationId);
                        setRecords([]);
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
                    onChange={(event) =>
                        setFrom(event.target.value)
                    }
                />

                <input
                    type="date"
                    value={to}
                    onChange={(event) =>
                        setTo(event.target.value)
                    }
                />

                <button type="button" onClick={loadWeather}>
                    Search
                </button>
            </div>

            {error && (
                <p style={{ color: "darkred" }}>
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
                        <th style={cellStyle}>Rain</th>
                        <th style={cellStyle}>Snowfall</th>
                        <th style={cellStyle}>Snow depth</th>
                    </tr>
                </thead>

                <tbody>
                    {records.map((record) => (
                        <tr
                            key={`${record.stationId}-${record.date}`}
                        >
                            <td style={cellStyle}>
                                {record.date}
                            </td>

                            <td style={cellStyle}>
                                {record.stationId}
                            </td>

                            <td style={cellStyle}>
                                {formatInches(
                                    record.precipitationInches
                                )}
                            </td>

                            <td style={cellStyle}>
                                {formatInches(
                                    record.snowfallInches
                                )}
                            </td>

                            <td style={cellStyle}>
                                {formatInches(
                                    record.snowDepthInches
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {records.length === 0 && !error && (
                <p style={{ marginTop: "30px" }}>
                    No weather records loaded.
                </p>
            )}
        </main>
    );
}

function formatInches(value: number | null): string {
    if (value === null) {
        return "—";
    }

    return `${value.toFixed(2)} in`;
}

const cellStyle = {
    border: "1px solid #ccc",
    padding: "10px",
    textAlign: "left" as const,
};

export default App;