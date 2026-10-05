# PulsePass — Capa de persistencia

Implementación del PRD de PulsePass v1.0: modelo relacional, migraciones Flyway,
entidades JPA, repositories Spring Data y pruebas de integración contra PostgreSQL
real con Testcontainers.

| Ítem        | Valor                                    |
|-------------|------------------------------------------|
| Java        | 21                                       |
| Framework   | Spring Boot 4.x                          |
| Build       | Maven                                    |
| Persistencia| Spring Data JPA / Hibernate              |
| Base de datos | PostgreSQL (Testcontainers en pruebas) |
| Esquema     | Flyway (`ddl-auto=validate`)             |

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

### Aplicación contra una base local (opcional)

```bash
docker run --name pulsepass-db -e POSTGRES_DB=pulsepass \
  -e POSTGRES_USER=pulsepass -e POSTGRES_PASSWORD=pulsepass \
  -p 5432:5432 -d postgres:16-alpine

mvn spring-boot:run
```

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

## 5. Pruebas

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

## 6. Estructura

```
src/main/java/com/pulsepass/
├── PulsePassApplication.java
├── domain/           Venue, Event, Artist, User, UserProfile, Ticket
│   └── enums/        EventCategory, EventStatus, TicketType, TicketStatus
└── repository/       6 repositories JpaRepository
src/main/resources/
├── application.properties
└── db/migration/     V1, V2, V3
src/test/java/com/pulsepass/
├── support/          AbstractPostgresIT, TestData
└── repository/       7 clases de prueba
```

---

## 7. Fuera de alcance

Según las secciones 2.4 y 18 del PRD, este proyecto no incluye capa Service, API
REST, autenticación, pagos, inventario por sector ni control de concurrencia. El
estado `SOLD_OUT` se persiste manualmente; no se infiere (BR-010, R-002).
