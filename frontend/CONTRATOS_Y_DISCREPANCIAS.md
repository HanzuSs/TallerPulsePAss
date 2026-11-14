# Contratos verificados y discrepancias

Se inspeccionó la rama `main` del backend (commit `dba16f49f5a6c8b7e4719ad60ebf0cc25cfce7ca`) antes de implementar el cliente.

## Contratos confirmados

- Autenticación: `POST /api/auth/login` devuelve `accessToken`, `tokenType` y `expiresIn`; `GET /api/auth/me` devuelve `id`, `username`, `email` y `roles`.
- Usuario: registro incluye `username`, `email`, `firstName`, `lastName`, `phone`, `city`, `birthDate` y `password`; esta última debe tener entre 12 y 72 caracteres y no superar 72 bytes UTF-8.
- Eventos: `EventResponse` contiene `venueCode`, `venueName` y una lista de `ArtistResponse`; los listados usan `EventSummaryResponse`.
- Tickets: compra recibe `userEmail`, `eventCode` y `type`; el precio llega únicamente en `TicketResponse`. Cancelar y usar son `PATCH` sin cuerpo.
- Errores: `ErrorResponse` tiene `status`, `message` y `details`, que el cliente traduce a `ApiError` y a errores de campo.

## Diferencias o restricciones relevantes

| Tema | PRD / expectativa visual | Contrato real y resolución en el cliente |
| --- | --- | --- |
| Titular de ticket | Algunos diseños muestran nombre completo. | `TicketResponse` solo entrega `userEmail`; la interfaz muestra el correo, sin inferir nombre. |
| Listado ADMIN | El panel sugiere gestión de eventos. | No hay `GET /api/events` general. El panel busca por código y conserva los creados durante la pestaña en `sessionStorage`. |
| Inventario | El detalle no informa entradas restantes. | Se muestra estado del evento y la capacidad de la sede; capacidad/agotamiento se resuelven con el 409 de compra. |
| CORS | El PRD marca el riesgo. | No hay configuración CORS en el backend. Vite usa el proxy de desarrollo; producción necesita mismo origen o CORS del servidor. |
| Borradores | Visitantes no deben verlos. | `GET /api/events/{code}` devuelve 403 para no publicados a visitantes; la UI traduce 403/404 a “Evento no disponible”. |
| Cierre de sesión | No existe endpoint de logout. | El cierre es local: borra JWT y caché de TanStack Query. |
| Token | Sin refresh token. | Se programa cierre local con `expiresIn` y todo 401 vuelve a `/login`. |

No se modificó ningún archivo de `src/main` ni se agregó lógica de negocio al cliente.
