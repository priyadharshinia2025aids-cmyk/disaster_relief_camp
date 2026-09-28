# ReliefTrack — Run First

## Requirements
- Java 17 or later
- IntelliJ IDEA
- MySQL Server running
- MySQL user: `root`
- MySQL password: `root`

## Run in IntelliJ
1. Extract this ZIP.
2. Open the folder that contains `pom.xml` in IntelliJ IDEA.
3. Wait for Maven dependencies to finish downloading.
4. Open `src/main/java/com/reliefcamp/tracker/ReliefCampApplication.java`.
5. Click the green Run button.
6. The application uses port `8081`.
7. Open `http://localhost:8081/`.

## MySQL
The application creates `relief_camp_db` automatically when MySQL is running because the JDBC URL contains `createDatabaseIfNotExist=true`.

If your MySQL password is not `root`, change only `spring.datasource.password` in `src/main/resources/application.properties`.

## Demo data
On the first empty database, the application inserts a small demo dataset so the frontend is not blank. It will not insert duplicates on later starts.

## API
- `GET /api/dashboard`
- `GET/POST/PUT/DELETE /api/camps`
- `GET/POST/PUT/DELETE /api/supplies`
- `GET/POST/PUT/DELETE /api/families`
- `PUT /api/families/{id}/check-in`
- `PUT /api/families/{id}/check-out`
- `GET/POST/DELETE /api/distributions`
- Swagger: `http://localhost:8081/swagger-ui.html`

## IntelliJ structure
Use the Packages view if you want IntelliJ to show class/interface icons:
Project panel → dropdown → Packages.

- Blue C = class
- Green I = interface
- Repository files are interfaces.
- No DTO package is used.
