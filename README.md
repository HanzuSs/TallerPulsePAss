# PulsePass — Persistencia, servicios y API REST

Caso de estudio académico implementado por fases:

| Fase | Contenido |
|------|-----------|
| 1 — Persistencia | Modelo relacional, migraciones Flyway, entidades JPA, repositories Spring Data y pruebas de integración contra PostgreSQL real con Testcontainers. |
| 2 — Servicios | Interfaces e implementaciones de Service, DTOs, mappers MapStruct, reglas de negocio, transacciones y excepciones propias. |
| 3 — Controladores REST | Endpoints HTTP, validación de entrada con Bean Validation, manejo global de errores y pruebas de contrato con MockMvc. |

| Ítem        | Valor                                    |
|-------------|------------------------------------------|
| Java        | 21                                       |
| Framework   | Spring Boot 4.x                          |
| Build       | Maven                                    |
| API REST    | Spring MVC (`@RestController`) + Bean Validation |
| Persistencia| Spring Data JPA / Hibernate              |
| Base de datos | PostgreSQL (Testcontainers en pruebas) |
| Esquema     | Flyway (`ddl-auto=validate`)             |
| Pruebas de Controller | `@WebMvcTest`, MockMvc y Mockito (`@MockitoBean`) |

---

## 1. Cómo ejecutar

### Pruebas (no requiere instalar PostgreSQL)

```bash
mvn clean test
```

Solo hace falta un Docker en ejecución. Testcontainers levanta `postgres:16-alpine`,
Flyway construye el esquema desde cero e Hibernate lo valida. **No se usa H2** (NFR-005).

> Surefire está configurado para incluir también las clases `*IT.java`, de modo que
> `mvn clean test` ejecuta toda la suite del taller en un solo comando (QT-010).

Para ejecutar **solo las pruebas de la capa de controladores** (no necesitan Docker ni
PostgreSQL, ver sección 7):

```bash
mvn test "-Dtest=*Controller*Test"
```

### Aplicación contra una base local (opcional)

```bash
docker run --name pulsepass-db -e POSTGRES_DB=pulsepass \
  -e POSTGRES_USER=pulsepass -e POSTGRES_PASSWORD=pulsepass \
  -p 5432:5432 -d postgres:16-alpine

mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080` (ver sección 6).

---

## 2. Modelo de dominio

```
Venue 1 ──── N Event
Event N ──── M Artist      (tabla event_artists, PK compuesta)
User  1 ──── 1 UserProfile (FK UNIQUE en user_profiles.user_id)
User  1 ──── N Ticket
Event 1 ──── N Ticket
```

Seis entidades en `com.pulsepass.domain`: `Venue`, `Event`, `Artist`, `User`,
`UserProfile`, `Ticket`, más los enums en `com.pulsepass.domain.enums`.

Decisiones de mapeo que vale la pena justificar:

| Decisión | Razón |
|----------|-------|
| `Ticket` es entidad, no `@ManyToMany` | Tiene atributos propios: `ticketCode`, `type`, `price`, `status`, `purchaseDate` (BR-006). |
| `@Enumerated(EnumType.STRING)` | El ordinal se rompe al reordenar el enum; el nombre es estable (BR-008). |
| `BigDecimal` + `NUMERIC(12,2)` | Precisión monetaria; `float`/`double` introducen error de redondeo (BR-007, NFR-008). |
| `Set<Artist>` en la N:M | Semántica de conjunto: el mismo par evento-artista no se repite, respaldado por la PK compuesta. |
| `Event` es el lado dueño de la N:M | `Artist` usa `mappedBy`, así existe una sola tabla de unión. |
| `UserProfile` es el lado dueño del 1:1 | La FK vive en `user_profiles` y lleva `UNIQUE`, que es lo que hace cumplir el 1:1 en la base. |
| `FetchType.LAZY` en todos los `@ManyToOne`/`@OneToOne` | Evita cargas en cascada no deseadas; el default `EAGER` del `@ManyToOne` es una trampa clásica. |
| `equals`/`hashCode` sobre la clave de negocio | Estables antes y después del `persist`, a diferencia del `id` generado. |
| Sin Lombok `@Data` | Genera `equals`/`hashCode`/`toString` que tocan relaciones lazy y provocan recursión (sección 13). |

---

## 3. Migraciones

| Archivo | Contenido |
|---------|-----------|
| `V1__create_schema.sql` | Las siete tablas con PK, FK, UNIQUE, CHECK e índices. |
| `V2__insert_initial_artists.sql` | Catálogo inicial: Solar Beat, Neon Waves, Caribbean Sound, Ocean Drive, Digital Pulse. |
| `V3__add_streaming_url_to_event.sql` | `streaming_url VARCHAR(500)` nullable sobre `events` (FR-EVT-006). |

V1 no se modifica una vez aplicada: Flyway guarda un checksum en
`flyway_schema_history` y cualquier cambio posterior rompe la validación en todo
ambiente donde ya corrió.

Restricciones reforzadas en PostgreSQL (NFR-001):

- `UNIQUE`: `venues.code`, `events.event_code`, `artists.stage_name`,
  `users.username`, `users.email`, `tickets.ticket_code`, `user_profiles.user_id`.
- `CHECK`: `capacity > 0`, `price >= 0`, `minimum_age >= 0`, y los catálogos de
  `category`, `status` (evento), `type` y `status` (ticket).
- `PRIMARY KEY (event_id, artist_id)` en `event_artists`.

---

## 4. Consultas

| Necesidad | Mecanismo | Dónde |
|-----------|-----------|-------|
| Evento por `eventCode` | Query Method | `EventRepository.findByEventCode` |
| Cartelera publicada ordenada | Query Method | `findByStatusOrderByEventDateAsc` |
| Eventos de un venue por `code` | Query Method con navegación | `findByVenue_Code` |
| Usuario por email sin distinguir mayúsculas | Query Method | `UserRepository.findByEmailIgnoreCase` |
| Tickets de un usuario por email y estado | Query Method con navegación | `TicketRepository.findByUser_EmailIgnoreCaseAndStatus` |
| Tickets PAID de un evento | Query Method | `findByEvent_EventCodeAndStatus` |
| Eventos por artista | JPQL + JOIN + DISTINCT | `findEventsByArtistStageName` |
| Eventos por ciudad y artista | JPQL con dos asociaciones | `findEventsByCityAndArtist` |
| Eventos recomendados | JPQL con filtros, `LIKE`, DISTINCT y orden | `findRecommendedEvents` |
| Conteo de ventas | JPQL + COUNT | `countPaidTicketsByEventCode` |

Criterio usado (NFR-007): el Query Method gana mientras el nombre del método siga
siendo legible. Apenas hacen falta un `JOIN` explícito, `DISTINCT`, una función o
más de dos asociaciones, se pasa a JPQL — `findByEvent_EventCodeAndStatus` está
justo en el límite y se dejó como Query Method porque es un filtro directo.

`findByEvent_EventCodeAndStatus` devuelve solo los PAID cuando se le pasa ese
estado; el `DISTINCT` de las consultas sobre la N:M es obligatorio porque el JOIN
multiplica la fila del evento por cada artista coincidente.

---

## 5. Pruebas de persistencia

| Clase | Cubre |
|-------|-------|
| `FlywayMigrationIT` | QT-001, QT-002: V1–V3 aplicadas, tablas presentes, `streaming_url` nullable, semilla de artistas. |
| `VenuePersistenceTest` | AC-001, FR-VEN-002/003: persistencia, UNIQUE de código, CHECK de capacidad. |
| `EventRepositoryIT` | QT-003, AC-002, AC-006: 1:N con Venue, enums por nombre, cartelera ordenada. |
| `EventArtistIT` | QT-005, AC-003, AC-007: N:M sin pares duplicados, JPQL sin duplicados. |
| `UserProfileIT` | QT-004, AC-004: 1:1 y rechazo del segundo perfil. |
| `TicketRepositoryIT` | QT-006, AC-005, AC-008: FKs, código único, CHECK de precio, conteo solo de PAID. |
| `EventSearchIT` | FR-SRC-001..004: ciudad + artista, recomendados case-insensitive, tickets de eventos futuros. |

Todas heredan de `AbstractPostgresIT`, que arranca un único contenedor PostgreSQL
para toda la suite y lo publica vía `@DynamicPropertySource`. Cada test corre en una
transacción que se revierte al terminar (`@DataJpaTest`), así que el orden de
ejecución no afecta los resultados.

Dos detalles deliberados: los CHECK de `capacity` y `price` se prueban por
`JdbcTemplate` y no por el repository, porque Bean Validation interceptaría el valor
antes de llegar a PostgreSQL y la prueba dejaría de demostrar lo que dice demostrar;
y las violaciones de UNIQUE se fuerzan con `saveAndFlush` para que el `INSERT` viaje
a la base dentro del test y no al cerrar la transacción (QT-009).

---

## 6. API REST (Fase 3)

Los Controllers solo traducen HTTP a llamadas de Service y devuelven DTOs; las reglas de
negocio viven en la capa Service y los errores se convierten en respuestas HTTP en un único
punto (`GlobalExceptionHandler`).

```
Cliente HTTP → Controller (@Valid) → Service → Repository → PostgreSQL
                    │                   │
                    └── ResponseEntity  └── excepción → GlobalExceptionHandler → ErrorResponse
```

Principios (PRD, sección 6): el Controller no tiene reglas de negocio, no accede a
Repository, no expone entidades JPA, no tiene bloques `try/catch` y todo error usa el mismo JSON.

### 6.1 Endpoints

Cubren las 20 operaciones públicas de los cinco Services. Los códigos de error de la última
columna son los que puede producir cada endpoint; todos usan `ErrorResponse` (sección 6.3).

| Método | Endpoint | Service | Éxito | Errores |
|--------|----------|---------|-------|---------|
| GET | `/api/venues/{code}` | `VenueService.findByCode` | 200 | 404 |
| GET | `/api/venues/active` | `VenueService.findActiveVenues` | 200 | — |
| POST | `/api/events` | `EventService.create` | 201 | 400, 404 (venue), 409 |
| GET | `/api/events/{eventCode}` | `EventService.findByCode` | 200 | 404 |
| GET | `/api/events/published` | `EventService.findPublishedEvents` | 200 | — |
| PATCH | `/api/events/{eventCode}/publish` | `EventService.publish` | 200 | 404, 409 |
| POST | `/api/events/{eventCode}/artists/{artistId}` | `EventService.addArtist` | 200 | 400, 404, 409 |
| GET | `/api/events/by-artist?stageName=...` | `EventService.findByArtist` | 200 | 400 |
| GET | `/api/artists/{id}` | `ArtistService.findById` | 200 | 400, 404 |
| GET | `/api/artists/by-stage-name?stageName=...` | `ArtistService.findByStageName` | 200 | 400, 404 |
| GET | `/api/artists/active` | `ArtistService.findActiveArtists` | 200 | — |
| POST | `/api/users` | `UserService.register` | 201 | 400, 409 |
| GET | `/api/users/by-email?email=...` | `UserService.findByEmail` | 200 | 400, 404 |
| GET | `/api/users/by-username?username=...` | `UserService.findByUsername` | 200 | 400, 404 |
| POST | `/api/tickets` | `TicketService.purchase` | 201 | 400, 404, 409 |
| GET | `/api/tickets/{ticketCode}` | `TicketService.findByCode` | 200 | 404 |
| GET | `/api/tickets/by-user?email=...` | `TicketService.findByUserEmail` | 200 | 400 |
| GET | `/api/events/{eventCode}/tickets/paid` | `TicketService.findPaidTicketsByEvent` | 200 | — |
| PATCH | `/api/tickets/{ticketCode}/cancel` | `TicketService.cancel` | 200 | 404, 409 |
| PATCH | `/api/tickets/{ticketCode}/use` | `TicketService.markAsUsed` | 200 | 404, 409 |

Notas:

- La creación (`POST /api/events`, `/api/users`, `/api/tickets`) responde **201 Created**; el resto de
  operaciones exitosas, incluidas las asociaciones y los cambios de estado, responden **200 OK**.
- `GET /api/events/{eventCode}/tickets/paid` cuelga del recurso evento, pero lo sirve
  `TicketController` porque es quien depende de `TicketService`.
- Las rutas literales (`/active`, `/published`, `/by-artist`, `/by-user`...) tienen prioridad sobre
  las plantillas (`/{code}`, `/{eventCode}`...), por eso no hay ambigüedad.
- No hay endpoint para crear venues (no está en el alcance del PRD): para probar el flujo completo
  se inserta uno por SQL (ver 6.5).

### 6.2 Validación de entrada (Bean Validation)

La validación **estructural** ocurre en el Controller (`@Valid`) antes de llamar al Service: si falla, el
Service nunca se invoca. Las **reglas de negocio** (usuario activo, evento publicado, edad mínima,
capacidad, transiciones de estado, duplicados) siguen en el Service.

| DTO | Reglas |
|-----|--------|
| `CreateEventRequest` | `eventCode`, `name`, `venueCode` obligatorios; `category`, `eventDate` y `minimumAge` no nulos; `minimumAge >= 0`; `description` máx. 1000 caracteres |
| `RegisterUserRequest` | `username`, `email` (formato válido), `firstName`, `lastName` obligatorios; `birthDate` no nula |
| `PurchaseTicketRequest` | `userEmail` obligatorio y con formato de email; `eventCode` obligatorio; `type` no nulo |

Las longitudes máximas coinciden con las columnas definidas en `V1__create_schema.sql`.
Un JSON mal formado, un body ausente, una fecha ilegible o un valor de enum desconocido responden 400.

### 6.3 Contrato de errores

Todo error HTTP de la aplicación tiene la misma forma (`ErrorResponse`):

```json
{
  "timestamp": "2026-10-04T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Event not found: CMF-2026",
  "details": {}
}
```

En errores de validación, `details` trae un par campo/mensaje por cada campo inválido:

```json
{
  "timestamp": "2026-10-04T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "eventCode": "Event code is required",
    "venueCode": "Venue code is required"
  }
}
```

| Excepción / condición | HTTP | Uso |
|-----------------------|------|-----|
| `MethodArgumentNotValidException` | 400 | Bean Validation |
| `HttpMessageNotReadableException` | 400 | JSON mal formado, body ausente, enum o fecha ilegibles |
| Parámetro obligatorio ausente / de tipo incorrecto | 400 | `?email=` faltante, `/api/artists/abc` |
| `ResourceNotFoundException` | 404 | Recurso inexistente |
| `DuplicateResourceException` | 409 | Conflicto de unicidad |
| `BusinessRuleException` | 409 | Regla de negocio incumplida |
| Método HTTP o `Content-Type` no soportado | 405 / 415 | Evita que Spring MVC caiga en el 500 genérico |
| Cualquier otra excepción | 500 | Mensaje genérico; el detalle solo va al log (NFR-CTRL-007) |

### 6.4 Request de ejemplo

`eventDate` debe ser una fecha futura: el Service rechaza fechas pasadas con 409.

```json
// POST /api/events
{
  "eventCode": "CMF-2026",
  "name": "Caribbean Music Fest 2026",
  "description": "Festival de música del Caribe",
  "category": "MUSIC",
  "eventDate": "2027-03-14T20:00:00",
  "minimumAge": 18,
  "venueCode": "VEN-SMR-01"
}
```

```json
// POST /api/users
{
  "username": "andrea",
  "email": "andrea@example.com",
  "firstName": "Andrea",
  "lastName": "Gomez",
  "phone": "+57 300 000 0000",
  "city": "Santa Marta",
  "birthDate": "1998-05-20"
}
```

```json
// POST /api/tickets   (type: GENERAL | VIP | BACKSTAGE | STUDENT)
{
  "userEmail": "andrea@example.com",
  "eventCode": "CMF-2026",
  "type": "GENERAL"
}
```

### 6.5 Probar la API a mano

Con la base local levantada (sección 1) y la aplicación corriendo, primero se inserta un venue
(los artistas ya los siembra Flyway en V2):

```bash
docker exec -i pulsepass-db psql -U pulsepass -d pulsepass -c \
  "INSERT INTO venues (code, name, city, address, capacity, active)
   VALUES ('VEN-SMR-01', 'Marina Convention Center', 'Santa Marta', 'Av. del Ferrocarril 10-25', 3, TRUE);"
```

Luego el flujo completo del caso de estudio:

```bash
curl -i http://localhost:8080/api/venues/VEN-SMR-01

curl -i -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"eventCode":"CMF-2026","name":"Caribbean Music Fest 2026","category":"MUSIC","eventDate":"2027-03-14T20:00:00","minimumAge":18,"venueCode":"VEN-SMR-01"}'

curl -i -X POST http://localhost:8080/api/events/CMF-2026/artists/1
curl -i -X PATCH http://localhost:8080/api/events/CMF-2026/publish
curl -i http://localhost:8080/api/events/published

curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"andrea","email":"andrea@example.com","firstName":"Andrea","lastName":"Gomez","birthDate":"1998-05-20"}'

curl -i -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"userEmail":"andrea@example.com","eventCode":"CMF-2026","type":"GENERAL"}'
```

(En Windows PowerShell conviene usar `curl.exe` o una herramienta como Postman; las comillas
del `-d` se escriben distinto.)

---

## 7. Pruebas de Controller

Cada Controller tiene su clase de pruebas con `@WebMvcTest`, MockMvc y el Service reemplazado por
`@MockitoBean`. **No se levanta PostgreSQL, ni Docker, ni el contexto completo de Spring**
(NFR-CTRL-002, NFR-CTRL-008): lo que se prueba es el contrato HTTP.

| Clase | Cubre |
|-------|-------|
| `VenueControllerTest` | TEST-CTRL-VEN-001..003, AC-CTRL001/002: 200, 404 con `ErrorResponse`, lista de activos. |
| `EventControllerTest` | TEST-CTRL-EVT-001..009, AC-CTRL003..006: 201, 400 con `details`, 404, 409, JSON mal formado, 405 y 500 sin detalles internos. |
| `ArtistControllerTest` | TEST-CTRL-ART-001..004: 200, 404, id no numérico y parámetro ausente (400). |
| `UserControllerTest` | TEST-CTRL-USR-001..005, AC-CTRL007/008: 201, 400, 409 sin exponer detalles internos, 415. |
| `TicketControllerTest` | TEST-CTRL-TKT-001..011, AC-CTRL009..014: compra, consultas y transiciones cancel/use con 200, 400, 404 y 409. |
| `ControllerArchitectureTest` | AC-CTRL015/016 y Definition of Done: sin Repository en los Controllers, sin entidades JPA en los endpoints, todo `ResponseEntity`, 20 operaciones de Service con endpoint. |

Qué se verifica en cada prueba (QT-CTRL-003..011): status HTTP, `Content-Type`, campos JSON con
`jsonPath`, la interacción con el Service (`verify`), que un request inválido **no** invoca el
Service (`verify(..., never())`) y la estructura de `ErrorResponse` (helper `ErrorResponseAssertions`).
Los datos de referencia (venue `VEN-SMR-01`, evento `CMF-2026`, artistas, Andrea) están en `ControllerTestData`.

---

## 8. Estructura

```
src/main/java/com/pulsepass/
├── PulsePassApplication.java
├── domain/           Venue, Event, Artist, User, UserProfile, Ticket
│   └── enums/        EventCategory, EventStatus, TicketType, TicketStatus
├── repository/       6 repositories JpaRepository
├── mapper/           5 mappers MapStruct (entidad a DTO)
├── dto/
│   ├── request/      CreateEventRequest, RegisterUserRequest, PurchaseTicketRequest
│   └── response/     VenueResponse, EventResponse, EventSummaryResponse, ArtistResponse,
│                     UserResponse, TicketResponse, ErrorResponse
├── service/          5 interfaces de Service
│   └── impl/         5 implementaciones con las reglas de negocio
├── controller/       VenueController, EventController, ArtistController,
│                     UserController, TicketController
└── exception/        ResourceNotFoundException, DuplicateResourceException,
                      BusinessRuleException, GlobalExceptionHandler
src/main/resources/
├── application.properties
└── db/migration/     V1, V2, V3
src/test/java/com/pulsepass/
├── support/          AbstractPostgresIT, TestData, ControllerTestData, ErrorResponseAssertions
├── repository/       7 clases de prueba (PostgreSQL + Testcontainers)
└── controller/       5 clases *ControllerTest + ControllerArchitectureTest (sin base de datos)
```

---

## 9. Fuera de alcance

Según los PRD de las tres fases, este proyecto no incluye autenticación ni autorización
(Spring Security, JWT, OAuth2), pagos, inventario por sector, control de concurrencia, paginación,
versionado de la API (`/api/v1`), OpenAPI/Swagger ni pruebas E2E. El estado `SOLD_OUT` lo asigna
`TicketService` cuando se agota la capacidad del venue; la capa de persistencia solo lo almacena.
