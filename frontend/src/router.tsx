import { createBrowserRouter } from 'react-router'
import { Layout } from './layouts/Layout'
import { RutaPorRol, RutaProtegida, SoloAnonimo } from './layouts/Guardas'
import Inicio from './pages/Inicio'
import ErrorPagina from './pages/ErrorPagina'

const carga = (cargar: () => Promise<{ default: React.ComponentType }>) => async () => ({ Component: (await cargar()).default })
export const router = createBrowserRouter([{ path: '/', Component: Layout, errorElement: <ErrorPagina/>, children: [
  { index: true, Component: Inicio },
  { path: 'eventos', lazy: carga(() => import('./pages/CatalogoEventos')) },
  { path: 'eventos/:eventCode', lazy: carga(() => import('./pages/DetalleEvento')) },
  { path: 'artistas', lazy: carga(() => import('./pages/ListaArtistas')) },
  { path: 'artistas/:id', lazy: carga(() => import('./pages/DetalleArtista')) },
  { path: 'sedes', lazy: carga(() => import('./pages/ListaSedes')) },
  { path: 'sedes/:code', lazy: carga(() => import('./pages/DetalleSede')) },
  { Component: SoloAnonimo, children: [{ path: 'login', lazy: carga(() => import('./pages/Login')) }, { path: 'registro', lazy: carga(() => import('./pages/Registro')) }] },
  { Component: RutaProtegida, children: [
    { path: 'cuenta', lazy: carga(() => import('./pages/Cuenta')) },
    { Component: () => <RutaPorRol roles={['USER']}/>, children: [{ path: 'mis-tickets', lazy: carga(() => import('./pages/MisTickets')) }, { path: 'mis-tickets/:ticketCode', lazy: carga(() => import('./pages/DetalleTicket')) }, { path: 'compra/confirmacion/:ticketCode', lazy: carga(() => import('./pages/ConfirmacionCompra')) }] },
    { Component: () => <RutaPorRol roles={['STAFF', 'ADMIN']}/>, children: [{ path: 'staff/validacion', lazy: carga(() => import('./pages/ValidacionIngreso')) }, { path: 'staff/eventos/:eventCode/tickets', lazy: carga(() => import('./pages/TicketsPagadosEvento')) }] },
    { Component: () => <RutaPorRol roles={['ADMIN']}/>, children: [{ path: 'admin/eventos', lazy: carga(() => import('./pages/AdminEventos')) }, { path: 'admin/eventos/nuevo', lazy: carga(() => import('./pages/AdminCrearEvento')) }, { path: 'admin/eventos/:eventCode', lazy: carga(() => import('./pages/AdminGestionEvento')) }] },
  ] },
  { path: '403', lazy: carga(() => import('./pages/SinPermisos')) },
  { path: '*', lazy: carga(() => import('./pages/NoEncontrada')) },
] }])
