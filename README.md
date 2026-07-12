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
