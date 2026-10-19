# Checklist E2E manual - orden de ejecucion

Marcar con `x` tras ejecutar y registrar el resultado en `registro-ejecucion-manual.md`. Verificar el token elegido antes de cada Send. Si un prerrequisito falla, marcar los dependientes BLOCKED.

## Preflight (bloqueante)

- [ ] App arranca contra la BD `pulsepass_e2e` y las migraciones Flyway estan aplicadas
- [ ] `fixtures/seed-e2e.sql` aplicado; STAFF, ADMIN e INACTIVE_USER provisionados con BCrypt (fuera de HTTP)
- [ ] Fases 3 y 4 implementadas (controladores, `/api/auth/login`, `/api/auth/me`) y `PULSEPASS_JWT_SECRET` configurada
- [ ] Politica 403 vs 404 para recurso ajeno decidida y cargada en `expectedForeignResourceStatus`
- [ ] Variables del ambiente completadas (runId, usuarios, passwords locales, codigos de fixtures, artistId)

## Flujo A - Descubrimiento publico (sin token)

- [x] E2E-PRE-01
- [ ] E2EVEN02
- [x] E2EVEN01
- [x] E2EVEN01-N
- [x] E2EART03
- [ ] E2EART01
- [ ] E2EART01-N
- [ ] E2EART02
- [ ] E2EEVT03
- [ ] E2EEVT02
- [ ] E2EEVT06

## Flujo B - Registro y JWT

- [ ] E2EUSR01-A
- [ ] E2EUSR01-B
- [ ] E2EUSR01-DUP
- [ ] E2EAUTH01-A
- [ ] E2EAUTH01-A2
- [ ] E2EAUTH01-B
- [ ] E2EAUTH01-S
- [ ] E2EAUTH01-D
- [ ] E2EAUTH02-A
- [ ] E2EAUTH02-B
- [ ] E2EAUTH02-S
- [ ] E2EAUTH02-D
- [ ] TC-AUTH-004
- [ ] TC-AUTH-005
- [ ] TC-AUTH-007
- [ ] TC-AUTH-008
- [ ] TC-AUTH-009
- [ ] TC-AUTH-010
- [ ] TC-AUTH-011
- [ ] TC-AUTH-012
- [ ] TC-AUTH-013
- [ ] TC-AUTH-014
- [ ] TC-AUTH-015
- [ ] TC-SEC-016

## Flujo C - Catalogo (ADMIN)

- [ ] E2EEVT01
- [ ] E2EEVT02-D
- [ ] E2EEVT05
- [ ] E2EEVT04
- [ ] E2EEVT03
- [ ] E2EEVT06-D
- [ ] TC-SEC-001a
- [ ] TC-SEC-001b
- [ ] TC-SEC-002a
- [ ] TC-SEC-002b
- [ ] TC-SEC-003
- [ ] TC-SEC-004
- [ ] TC-SEC-005
- [ ] TC-SEC-015

## Usuarios y propiedad

- [ ] E2EUSR02
- [ ] E2EUSR02-D
- [ ] E2EUSR03
- [ ] E2EUSR03-D
- [ ] TC-SEC-006
- [ ] TC-SEC-007

## Flujo D - Tickets (ownership ANTES de cancelar/usar)

- [ ] E2ETKT01
- [ ] E2ETKT01-C
- [ ] E2ETKT01-U
- [ ] E2ETKT02
- [ ] E2ETKT03
- [ ] TC-SEC-008
- [ ] TC-SEC-009
- [ ] TC-SEC-010
- [ ] TC-SEC-011
- [ ] TC-SEC-012
- [ ] TC-SEC-013
- [ ] E2ETKT04
- [ ] E2ETKT05
- [ ] FLUJO-D-01
- [ ] FLUJO-D-02
- [ ] E2ETKT06
- [ ] TC-SEC-014
- [ ] FLUJO-D-03
- [ ] FLUJO-D-04
- [ ] FLUJO-D-05
- [ ] TC-SEC-017

## Flujo E - Regresion de errores

- [ ] TC-HTTP-01
- [ ] TC-HTTP-02
- [ ] TC-HTTP-03
- [ ] TC-HTTP-04
- [ ] TC-HTTP-05
- [ ] TC-HTTP-06
- [ ] TC-HTTP-07
- [ ] TC-HTTP-08
- [ ] TC-BIZ-015a
- [ ] TC-BIZ-015b
- [ ] TC-BIZ-015c
- [ ] TC-BIZ-014a
- [ ] TC-BIZ-014b
- [ ] TC-BIZ-014c
