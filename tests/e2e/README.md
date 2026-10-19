# PulsePass - Pruebas E2E manuales con Postman (Fase 5)

Coleccion Postman v2.1 **sin scripts** para verificar manualmente las 22 rutas (20 de negocio + `POST /api/auth/login` + `GET /api/auth/me`), los roles USER/STAFF/ADMIN/anonimo y la propiedad de recursos. Cada request se envia individualmente con **Send**; no se usa Runner, Newman ni CLI.

## Estado de dependencias (leer primero)

El codigo entregado (`PWeb2T_Hanzu_Vives.zip`) contiene **Fases 1-2** (persistencia, servicios, DTOs, mappers). **No contiene** los paquetes de controladores ni de seguridad: no existen `/api/**`, `/api/auth/login`, `/api/auth/me`, `ErrorResponse`, el campo `password` en `RegisterUserRequest`, ni roles. Segun este PRD (DoR y FR-E2E-015), mientras esas fases no esten implementadas los casos deben registrarse como **BLOCKED**, nunca como PASS. La coleccion esta construida contra los contratos reales disponibles (DTOs y enums del codigo) y los contratos de Fases 3-4 del PRD; los puntos que dependen de ellas estan senalados abajo.

## Estructura

```
tests/e2e/
  README.md
  postman/PulsePass-Manual-E2E.postman_collection.json
  postman/PulsePass-local.postman_environment.json
  fixtures/README.md, fixtures/seed-e2e.sql
  checklist-e2e-manual.md
  registro-ejecucion-manual.md
  .gitignore
```

## Preparacion

1. Crear la base dedicada `pulsepass_e2e`; apuntar la app a ella (`spring.datasource.url`).
2. Configurar `PULSEPASS_JWT_SECRET_BASE64` (>= 32 bytes aleatorios, exclusivo de E2E) en el runtime.
3. Arrancar la app: Flyway aplica V1..Vn.
4. Ejecutar `fixtures/seed-e2e.sql` y provisionar STAFF, ADMIN e INACTIVE_USER (BCrypt, fuera de HTTP; ver `fixtures/README.md`).
5. Postman: importar coleccion y ambiente, seleccionar **PulsePass local**.
6. Completar variables: `runId`, usuarios, **passwords y identificadores privilegiados solo como valores locales (tipo secret)**, `artistId` (de `GET /api/artists/active`) y codigos de fixtures.
7. Definir `expectedForeignResourceStatus` (403 o 404) segun la politica de Fase 4.

## Ejecucion paso a paso

1. Enviar `E2E-PRE-01`. Si falla, todo queda BLOCKED.
2. Seguir el orden de `checklist-e2e-manual.md` (Flujos A-E).
3. Tras cada login, copiar **solo** `accessToken` a `userAToken` / `userBToken` / `staffToken` / `adminToken`. El token dura 15 min: repetir login si expira.
4. Copiar manualmente los codigos devueltos:

| Request | Copiar de la respuesta | A la variable |
|---|---|---|
| `E2EART03` | `id` de un artista | `artistId`, `artistStageName` |
| `E2EEVT01` | `eventCode` | `draftEventCode` (si se usa como borrador) |
| `E2ETKT01` | `ticketCode` | `ticketOwnershipCode` |
| `E2ETKT01-C` | `ticketCode` | `ticketCancelableCode` |
| `E2ETKT01-U` | `ticketCode` | `ticketUsableCode` |

5. Antes de cada request protegida, verificar en Authorization el token del actor correcto.
6. Revisar Status, Headers, Body y, en mutaciones, releer con un GET autorizado. Un 2xx por si solo no aprueba.
7. Registrar PASS / FAIL / BLOCKED / NOT RUN en `registro-ejecucion-manual.md`. Para cancelar/usar, **un ticket distinto por escenario**; las denegaciones por ownership se ejecutan **antes** de cancelar/usar.

## Casos con precondicion manual
- `TC-AUTH-009`: copiar `userAToken` a `tamperedToken` cambiando un caracter.
- `TC-AUTH-010` / `011`: `expiredToken` y `foreignKeyToken` se generan offline con una clave exclusiva de test; nunca con el secreto real.
- `TC-AUTH-013`: conservar un token previo a la desactivacion en `inactiveUserToken`.
- `TC-SEC-017`: degradar el rol de STAFF en la BD E2E conservando el token antiguo.
- `TC-SEC-016`: definir `userC*` en el ambiente; luego login de userC y `GET /api/auth/me`.

## Contratos reales usados (del codigo)
- Enums: `EventCategory` (MUSIC, SPORTS, TECHNOLOGY, EDUCATION, CULTURE, ENTERTAINMENT), `EventStatus`, `TicketType` (GENERAL, VIP, BACKSTAGE, STUDENT), `TicketStatus` (RESERVED, PAID, CANCELLED, USED).
- `CreateEventRequest`: eventCode, name, description, category, eventDate (ISO local), minimumAge, venueCode.
- `PurchaseTicketRequest`: userEmail, eventCode, type. La compra crea el ticket en estado PAID; GENERAL=100.00, VIP=150.00, BACKSTAGE=200.00, STUDENT=80.00.
- `RegisterUserRequest` actual no tiene `password`: el cuerpo de registro de la coleccion lo incluye segun Fase 4 y fallara/ignorara el campo hasta que exista.

## Pendiente de confirmar con Fases 3-4
- Codigos HTTP que mapean `BusinessRuleException` (fecha pasada, compra en borrador, edad, aforo): los casos `TC-BIZ-014*`/`015c` indican "segun mapeo"; anotar el real y fijarlo en la matriz.
- Politica 403 vs 404 para recursos ajenos.
- Nombres de campos de `/api/auth/me` y del `ErrorResponse`.

## Matriz de cobertura (22/22 rutas)

| # | Ruta | Caso nominal | Actor | HTTP |
|---|---|---|---|---|
| 1 | `GET /api/artists/active` | E2EART03 | ANON | 200 |
| 2 | `GET /api/artists/by-stage-name` | E2EART02 | ANON | 200 |
| 3 | `GET /api/artists/{id}` | E2EART01 | ANON | 200 |
| 4 | `GET /api/auth/me` | E2EAUTH02-A | USER_A | 200 |
| 5 | `GET /api/events/by-artist` | E2EEVT06 | ANON | 200 |
| 6 | `GET /api/events/published` | E2EEVT03 | ANON | 200 |
| 7 | `GET /api/events/{eventCode}` | E2EEVT02 | ANON | 200 |
| 8 | `GET /api/events/{eventCode}/tickets/paid` | E2ETKT04 | STAFF | 200 |
| 9 | `GET /api/tickets/by-user` | E2ETKT03 | USER_A | 200 |
| 10 | `GET /api/tickets/{ticketCode}` | E2ETKT02 | USER_A | 200 |
| 11 | `GET /api/users/by-email` | E2EUSR02 | USER_A | 200 |
| 12 | `GET /api/users/by-username` | E2EUSR03 | USER_A | 200 |
| 13 | `GET /api/venues/active` | E2EVEN02 | ANON | 200 |
| 14 | `GET /api/venues/{code}` | E2EVEN01 | ANON | 200 |
| 15 | `PATCH /api/events/{eventCode}/publish` | E2EEVT04 | ADMIN | 200 |
| 16 | `PATCH /api/tickets/{ticketCode}/cancel` | E2ETKT05 | USER_A | 200 |
| 17 | `PATCH /api/tickets/{ticketCode}/use` | E2ETKT06 | STAFF | 200 |
| 18 | `POST /api/auth/login` | E2EAUTH01-A | ANON | 200 |
| 19 | `POST /api/events` | E2EEVT01 | ADMIN | 201 |
| 20 | `POST /api/events/{eventCode}/artists/{artistId}` | E2EEVT05 | ADMIN | 200 |
| 21 | `POST /api/tickets` | E2ETKT01 | USER_A | 201 |
| 22 | `POST /api/users` | E2EUSR01-A | ANON | 201 |

Roles contrastados: anonimo, USER_A, USER_B, STAFF, ADMIN (casos `TC-SEC-*`, `TC-AUTH-*`). Total de requests en la coleccion: 89.

## Confidencialidad
No exportar el ambiente con tokens o passwords; los secretos van como valores locales. No adjuntar JWT ni passwords en la bitacora o capturas.
