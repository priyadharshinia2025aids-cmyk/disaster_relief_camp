# Relief Camp Resource Tracker (Simplified)

A Spring Boot + MySQL application for tracking disaster-relief camp capacity, incoming supplies, registered families and supply distributions.

## Requirements covered

- Register a relief camp with capacity.
- Log incoming supplies (food, water, blankets, medical/hygiene kits and other stock) with quantity and unit.
- Register families with household head, member count and contact number.
- Check families into a selected camp while enforcing remaining capacity.
- Check families out and automatically release camp capacity.
- Distribute supplies to checked-in families and automatically reduce inventory.
- Prevent a distribution when requested quantity exceeds current inventory.
- Prevent a distribution when the family is not checked in at the selected camp.
- Dashboard for current inventory units, camp count, family count and today's distributions.
- Service-layer business rules before persistence.
- Bean Validation for incoming requests.
- Global exception handling with clear JSON error messages.
- Console notifications for check-in/check-out/distribution status changes.
- Pagination and sorting for camp listing.
- Swagger/OpenAPI documentation.
- Responsive UI served directly by Spring Boot.

## Technology

- Java 17+
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL Connector/J
- Springdoc OpenAPI / Swagger UI
- HTML, CSS and vanilla JavaScript frontend

## IntelliJ IDEA setup

1. Install JDK 17 or newer.
2. Install MySQL and start the MySQL server.
3. Create a MySQL user/database if needed. The default project configuration is:
   - Database: `relief_camp_db`
   - Username: `root`
   - Password: `root`
   - Port: `3306`
4. Open the extracted folder in IntelliJ IDEA.
5. IntelliJ should detect `pom.xml` as a Maven project. Allow it to download dependencies.
6. Open `src/main/java/com/reliefcamp/tracker/ReliefCampApplication.java` and run it.
7. Open `http://localhost:8081/` for the UI.
8. Open `http://localhost:8081/swagger-ui.html` for API testing/documentation.

### If your MySQL password is different
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

The database is created automatically because the JDBC URL contains `createDatabaseIfNotExist=true`.

## Postman test order

### 1. Create camp
POST `http://localhost:8081/api/camps`

```json
{
  "name": "Pollachi Relief Camp",
  "location": "Municipal Community Hall",
  "capacity": 100
}
```

### 2. Add supply
POST `http://localhost:8081/api/supplies`

```json
{
  "campId": 1,
  "type": "WATER",
  "quantity": 500,
  "unit": "litres"
}
```

### 3. Register family
POST `http://localhost:8081/api/families`

```json
{
  "headName": "Ravi Kumar",
  "memberCount": 4,
  "contactNumber": "9876543210"
}
```

### 4. Check in family
PUT `http://localhost:8081/api/families/1/check-in`

```json
{
  "campId": 1
}
```

### 5. Distribute supply
POST `http://localhost:8081/api/distributions`

```json
{
  "campId": 1,
  "familyId": 1,
  "supplyId": 1,
  "quantity": 20
}
```

### 6. Verify inventory
GET `http://localhost:8081/api/supplies?campId=1`

The water quantity should now be 480.

### 7. Test the business-rule edge case
Try distributing more than the available quantity. The API returns HTTP 400 with a clear message and does not save the invalid distribution.

### 8. Test capacity
Create a camp with a small capacity, register a family whose member count is larger than the remaining capacity, then call check-in. The service rejects the request before changing the database.

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Dashboard summary |
| GET/POST | `/api/camps` | List / register camps |
| GET/PUT/DELETE | `/api/camps/{id}` | Camp details / update / delete |
| GET/POST | `/api/supplies` | List / log supplies |
| GET/PUT/DELETE | `/api/supplies/{id}` | Supply details / update / delete |
| GET/POST | `/api/families` | List / register families |
| GET/PUT/DELETE | `/api/families/{id}` | Family details / update / delete |
| PUT | `/api/families/{id}/check-in` | Check in family |
| PUT | `/api/families/{id}/check-out` | Check out family |
| GET/POST | `/api/distributions` | List / create distribution |
| DELETE | `/api/distributions/{id}` | Reverse distribution and restore stock |

## Suggested database relationships

- Camp 1 → many Supply records
- Camp 1 → many Family records
- Camp 1 → many Distribution records
- Family 1 → many Distribution records
- Supply 1 → many Distribution records

A supply type is unique per camp so one camp has one current inventory record for each supply type.

## Important implementation detail

The inventory and capacity rules are implemented in the service layer, not left to the database alone. Distribution runs in a transaction and locks the selected supply row before checking and reducing its quantity. This prevents the common "check quantity in controller, then update later" mistake.

## Simple project structure
This version intentionally does **not** use a DTO package. It follows the basic structure:

- `model` – database/entity classes and validation fields
- `controller` – REST API endpoints
- `service` – business rules and application logic
- `repository` – MySQL/JPA database access
- `exception` – error handling

The request bodies use the model classes directly, so the project is easier to follow for a beginner Spring Boot project.


## UI / UX
The frontend is a responsive ReliefTrack operations workspace with Home, Dashboard, Relief Camps, Inventory, Families, Distributions, Reports, Alerts and Settings. It uses the existing REST APIs directly and does not use DTO classes.
