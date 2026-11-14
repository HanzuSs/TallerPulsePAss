# PulsePass Web

SPA de PulsePass construida para el backend Spring Boot existente. Consume sus 20 operaciones de negocio y la autenticación JWT sin alterar reglas, DTO ni seguridad del servidor.

## Requisitos

- Node.js 22.22 o superior (React Router 8 lo exige).
- Backend de este repositorio ejecutándose en `http://localhost:8080`.
- PostgreSQL y las variables del backend configuradas como indica el README raíz.

## Instalación y ejecución

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Vite publica el cliente en `http://localhost:5173` y redirige `/api` al backend local. `VITE_API_BASE_URL=/api` es público y no contiene secretos. Para usar otro origen, cambie únicamente esa variable y asegure CORS en el servidor o sirva ambos desde el mismo origen.

## Calidad

```powershell
npm run lint
npm test
npm run build
```

La suite usa Vitest, Testing Library y MSW. Cubre el cliente HTTP, el Bearer JWT, el mapeo de errores, formato COP, estado vacío del catálogo y que un registro inválido no dispara red. Las páginas, formularios y guardas están preparadas para ampliar los escenarios de la matriz del PRD.

## Rutas

| Área | Rutas |
| --- | --- |
| Pública | `/`, `/eventos`, `/eventos/:eventCode`, `/artistas`, `/artistas/:id`, `/sedes`, `/sedes/:code` |
| Acceso | `/login`, `/registro`, `/cuenta` |
| USER | `/mis-tickets`, `/mis-tickets/:ticketCode`, `/compra/confirmacion/:ticketCode` |
| STAFF / ADMIN | `/staff/validacion`, `/staff/eventos/:eventCode/tickets` |
| ADMIN | `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode` |
| Errores | `/403`, cualquier ruta no definida |

## Decisiones de implementación

- React 19, TypeScript `strict`, Tailwind CSS v4, React Router v8 (sin `react-router-dom`), TanStack Query, React Hook Form y zod.
- Los DTO se validan con esquemas zod al entrar por HTTP; los tipos se derivan de ellos.
- El JWT solo vive en memoria y `sessionStorage`; jamás en URL, cookies o `localStorage`. Un 401 limpia sesión y caché.
- Los formularios validan estructura; edad, capacidad, propiedad y transiciones se delegan al backend.
- Carga perezosa para todas las páginas salvo Inicio y Layout, estados de carga/vacío/error, navegación por rol y modo oscuro manual.

## Restricciones conocidas de la API

Consulta [CONTRATOS_Y_DISCREPANCIAS.md](./CONTRATOS_Y_DISCREPANCIAS.md) para el contraste entre el PRD y `main`.
