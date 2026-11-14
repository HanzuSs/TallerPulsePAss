import { Navigate, Outlet, useLocation } from 'react-router'
import { Cargando } from '../componentes/ui'
import { RUTAS } from '../lib/rutas'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import type { Rol } from '../funcionalidades/catalogo/tipos'

export function RutaProtegida() { const { sesion } = useAuth(); const ubicacion = useLocation(); if (sesion.estado === 'cargando') return <Cargando/>; return sesion.estado === 'anonimo' ? <Navigate to={RUTAS.login} replace state={{ desde: `${ubicacion.pathname}${ubicacion.search}` }}/> : <Outlet/> }
export function RutaPorRol({ roles }: { roles: Rol[] }) { const { sesion } = useAuth(); if (sesion.estado !== 'autenticado') return <Navigate to={RUTAS.login} replace/>; return roles.some((rol) => sesion.usuario.roles.includes(rol)) ? <Outlet/> : <Navigate to={RUTAS.sinPermisos} replace/> }
export function SoloAnonimo() { const { sesion } = useAuth(); if (sesion.estado === 'cargando') return <Cargando/>; return sesion.estado === 'autenticado' ? <Navigate to={RUTAS.inicio} replace/> : <Outlet/> }
