# Fixtures E2E

| Fixture | Valor por defecto (runId r001) | Origen |
|---|---|---|
| Venue activo | `VEN-E2E-001` | `seed-e2e.sql` |
| Venue inactivo | `VEN-E2E-INACTIVO` | `seed-e2e.sql` |
| Artistas | `Solar Beat`, `Neon Waves`, ... | Flyway V2 (obtener `artistId` con `GET /api/artists/active`) |
| Evento publicado | `EVT-E2E-PUB-r001` | `seed-e2e.sql` |
| Evento borrador | `EVT-E2E-DRAFT-r001` | `seed-e2e.sql` |
| Evento por publicar | `EVT-E2E-TOPUB-r001` | `seed-e2e.sql` |
| Evento con edad minima 60 | `EVT-E2E-AGE-r001` | `seed-e2e.sql` |
| Evento agotado | `EVT-E2E-SOLD-r001` | `seed-e2e.sql` |
| USER_A / USER_B / userC | `usera_r001`, `userb_r001`, `userc_r001` | Registro publico (`POST /api/users`) |
| STAFF / ADMIN / INACTIVE_USER | - | **Pendiente**: provisionar con BCrypt segun el esquema de la Fase 4 |

## Reglas
- Usar solo la base `pulsepass_e2e`; el seed aborta si el nombre difiere.
- Orden: migraciones Flyway -> `seed-e2e.sql` -> usuarios privilegiados.
- Repetir una sesion: restaurar `pulsepass_e2e` y reaplicar, o cambiar `runId` y los codigos/correos del ambiente.
- Las fechas de eventos deben ser futuras; revisar `2027-12-15` antes de cada ciclo.
- Dominio reservado `example.test`; sin datos personales reales.
