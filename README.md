## Download CSV Data

The NOAA CSV files are too large to store in GitHub.

Download them from Google Drive:

[Download NOAA CSV files](https://drive.google.com/drive/folders/1OHrsX5inhkEmtPmVCcguEEf7_YZksAvs?usp=sharing)

Place the downloaded station file in:

src/main/resources/csv/station/

Place the downloaded weather files in:

src/main/resources/csv/weather/

For .gz files, unzip them with:

``gunzip src/main/resources/csv/station/*.gz``
``gunzip src/main/resources/csv/weather/*.gz``

For .zip files, use:

```bash
unzip "file.zip" -d src/main/resources/csv/station/
```
```bash
``unzip "file.zip" -d src/main/resources/csv/weather/``
```

## Run with Docker

NOAA runs with three containers:

* React frontend
* Spring Boot backend
* PostgreSQL database

### Build the backend

```bash
./mvnw clean package -DskipTests
```

### Build the frontend

```bash
cd frontend
npm install
npm run build
cd ..
```

### Start the application

```bash
docker compose up --build
```

Open the app at:

```text
http://localhost:5173
```

Backend API:

```text
http://localhost:8080
```

### Stop the application

```bash
docker compose down
```

PostgreSQL data stays saved in the Docker volume.

Avoid the following command unless you want to delete the database:

```bash
docker compose down -v
```

Check logs of all service or particular service.

Sample: docker compose logs backend-1

```bash
docker compose logs
```