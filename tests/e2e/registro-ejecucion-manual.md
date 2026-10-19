# Registro de ejecucion manual - PulsePass E2E

> Plantilla sin ejecutar. Todos los casos estan en NOT RUN. Rellenar tras cada **Send**. No pegar JWT, passwords ni datos privados.
> Resultados validos: PASS / FAIL / BLOCKED / NOT RUN. Un 2xx solo no implica PASS: revisar JSON y estado persistido.

**Fecha de sesion:** pendiente | **runId:** pendiente | **Ejecutor:** pendiente | **BD:** pulsepass_e2e

| Caso | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observacion |
|---|---|---|---|---|---|---|---|
| E2E-PRE-01 | ANON | GET /api/venues/active | 200 | 200 | Arreglo JSON con VEN-E2E-001, capacity 500, active true | PASS | Captura de Postman: 200 OK, 131 ms |
| E2EVEN01 | ANON | GET /api/venues/{{venueActiveCode}} | 200 | 200 | Devolvio VEN-E2E-001 con capacity 500 | PASS | Sin observaciones |
| E2EVEN01-N | ANON | GET /api/venues/NO-EXISTE-{{runId}} | 404 | 404 | ErrorResponse: timestamp, status=404, error=Not Found, message=Venue not found: NO-EXISTE-r001, details={} | PASS | 5 campos presentes; sin stack trace |
| E2EVEN02 | ANON | GET /api/venues/active | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EART01 | ANON | GET /api/artists/{{artistId}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EART01-N | ANON | GET /api/artists/999999999 | 404 | pendiente | pendiente | NOT RUN | pendiente |
| E2EART02 | ANON | GET /api/artists/by-stage-name | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EART03 | ANON | GET /api/artists/active | 200 | 200 | Arreglo de artistas activos (vistos: Ocean Drive id 4, Solar Beat id 1; todos active=true) | PASS | Solo se reviso parte del cuerpo; verificar los 5 artistas de V2 |
| E2EUSR01-A | ANON | POST /api/users | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR01-B | ANON | POST /api/users | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH01-A | ANON | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH01-A2 | ANON | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH01-B | ANON | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH01-S | ANON | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH01-D | ANON | POST /api/auth/login | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH02-A | USER_A | GET /api/auth/me | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH02-B | USER_B | GET /api/auth/me | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH02-S | STAFF | GET /api/auth/me | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EAUTH02-D | ADMIN | GET /api/auth/me | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR02 | USER_A | GET /api/users/by-email | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR02-D | ADMIN | GET /api/users/by-email | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR03 | USER_A | GET /api/users/by-username | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR03-D | ADMIN | GET /api/users/by-username | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT01 | ADMIN | POST /api/events | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT02 | ANON | GET /api/events/{{publishedEventCode}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT02-D | ADMIN | GET /api/events/{{draftEventCode}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT03 | ANON | GET /api/events/published | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT04 | ADMIN | PATCH /api/events/{{eventToPublishCode}}/publish | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT05 | ADMIN | POST /api/events/{{eventToPublishCode}}/artists/{{artistId}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT06 | ANON | GET /api/events/by-artist | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2EEVT06-D | ADMIN | GET /api/events/by-artist | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT01 | USER_A | POST /api/tickets | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT01-C | USER_A | POST /api/tickets | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT01-U | USER_A | POST /api/tickets | 201 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT02 | USER_A | GET /api/tickets/{{ticketOwnershipCode}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT03 | USER_A | GET /api/tickets/by-user | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT04 | STAFF | GET /api/events/{{publishedEventCode}}/tickets/paid | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT05 | USER_A | PATCH /api/tickets/{{ticketCancelableCode}}/cancel | 200 | pendiente | pendiente | NOT RUN | pendiente |
| E2ETKT06 | STAFF | PATCH /api/tickets/{{ticketUsableCode}}/use | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001a | ANON | POST /api/events | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001b | USER_A | POST /api/events | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002a | USER_A | PATCH /api/events/{{eventToPublishCode}}/publish | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002b | STAFF | PATCH /api/events/{{eventToPublishCode}}/publish | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-003 | USER_A | POST /api/events/{{eventToPublishCode}}/artists/{{artistId}} | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-004 | ANON | GET /api/events/{{draftEventCode}} | denegacion definida (401/403/404) | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-005 | ANON | GET /api/events/by-artist | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-006 | USER_B | GET /api/users/by-email | 403/404* segun expectedForeignResourceStatus | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-007 | USER_B | GET /api/users/by-username | 403/404* segun expectedForeignResourceStatus | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-009 | USER_B | GET /api/tickets/{{ticketOwnershipCode}} | 403/404* segun expectedForeignResourceStatus | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-010 | USER_B | GET /api/tickets/by-user | 403/404* segun expectedForeignResourceStatus | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-011 | USER_B | PATCH /api/tickets/{{ticketOwnershipCode}}/cancel | 403/404* segun expectedForeignResourceStatus | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-012 | USER_B | PATCH /api/tickets/{{ticketOwnershipCode}}/use | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-013 | USER_A | GET /api/events/{{publishedEventCode}}/tickets/paid | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-008 | USER_A | POST /api/tickets | denegado segun Fase 4 (no asumir 403) | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-014 | STAFF | PATCH /api/tickets/{{ticketUsableCode}}/use | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-015 | ADMIN | GET /api/events/{{draftEventCode}} | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016 | ANON | POST /api/users | 201 (rol ignorado) o 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-017 | STAFF | GET /api/events/{{publishedEventCode}}/tickets/paid | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-004 | ANON | POST /api/auth/login | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-005 | ANON | POST /api/auth/login | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-007 | ANON | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-008 | ANON | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-009 | ANON | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-010 | ANON | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-011 | ANON | GET /api/auth/me | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-012 | ANON | GET /api/tickets/by-user | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-013 | ANON | GET /api/auth/me | 401 / denegacion configurada | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-014 | USER_A | PATCH /api/events/{{eventToPublishCode}}/publish | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-015 | ANON | GET /api/venues/active | segun politica definida | pendiente | pendiente | NOT RUN | pendiente |
| E2EUSR01-DUP | ANON | POST /api/users | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-01 | ANON | POST /api/users | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-02 | ANON | POST /api/users | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-03 | ANON | POST /api/users | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-04 | ADMIN | POST /api/events | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015a | ADMIN | POST /api/events | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015b | ADMIN | POST /api/events | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015c | ADMIN | POST /api/events | segun mapeo de BusinessRuleException en Fase 3 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014a | USER_A | POST /api/tickets | segun mapeo de BusinessRuleException en Fase 3 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014b | USER_A | POST /api/tickets | segun mapeo de BusinessRuleException en Fase 3 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014c | USER_A | POST /api/tickets | segun mapeo de BusinessRuleException en Fase 3 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-05 | USER_A | POST /api/tickets | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-06 | ADMIN | GET /api/events/NO-EXISTE-{{runId}} | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-07 | ADMIN | GET /api/tickets/NO-EXISTE-{{runId}} | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-08 | ADMIN | POST /api/events/{{eventToPublishCode}}/artists/999999999 | 404 | pendiente | pendiente | NOT RUN | pendiente |
| FLUJO-D-01 | USER_A | PATCH /api/tickets/{{ticketCancelableCode}}/cancel | 409 | pendiente | pendiente | NOT RUN | pendiente |
| FLUJO-D-02 | STAFF | PATCH /api/tickets/{{ticketCancelableCode}}/use | 409 | pendiente | pendiente | NOT RUN | pendiente |
| FLUJO-D-03 | STAFF | PATCH /api/tickets/{{ticketUsableCode}}/use | 409 | pendiente | pendiente | NOT RUN | pendiente |
| FLUJO-D-04 | USER_A | PATCH /api/tickets/{{ticketUsableCode}}/use | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLUJO-D-05 | USER_A | GET /api/tickets/{{ticketUsableCode}} | 200 | pendiente | pendiente | NOT RUN | pendiente |

## Consolidacion (completar al cerrar)

| Modulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 00 Preparacion / conectividad | 1 | 0 | 0 | 0 |
| 01 Venues (2 rutas) | 2 | 0 | 0 | 1 |
| 02 Artists (3 rutas) | 1 | 0 | 0 | 3 |
| 03 Auth / usuarios (registro, login, me) | 0 | 0 | 0 | 13 |
| 04 Usuarios / consulta y propiedad (2 rutas) | 0 | 0 | 0 | 4 |
| 05 Events / administracion y visibilidad (6 rutas) | 0 | 0 | 0 | 8 |
| 06 Tickets / compra, consulta y ciclo de vida (6 rutas) | 0 | 0 | 0 | 8 |
| 07 Seguridad / escenarios negativos | 0 | 0 | 0 | 18 |
| 08 Validaciones y errores HTTP | 0 | 0 | 0 | 25 |
| 09 Flujos de negocio / secuencia recomendada | 0 | 0 | 0 | 5 |

## Defectos y bloqueos

| ID | Caso | Precondiciones | Pasos | HTTP esperado | HTTP observado | Tipo (defecto/bloqueo) |
|---|---|---|---|---|---|---|
| - | - | - | - | - | - | - |
