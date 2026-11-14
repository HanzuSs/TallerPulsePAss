/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { z } from 'zod'
import { establecerManejador401, establecerToken } from '../../lib/http'
import { queryClient } from '../../lib/queryClient'
import { loginApi, sesionApi } from './api'
import type { AuthenticatedUser, Rol } from '../catalogo/tipos'

type Sesion = { estado: 'cargando' } | { estado: 'anonimo' } | { estado: 'autenticado'; token: string; expiraEn: number; usuario: AuthenticatedUser }
type AuthValue = { sesion: Sesion; iniciarSesion: (identifier: string, password: string) => Promise<void>; cerrarSesion: (expirada?: boolean) => void; tieneRol: (...roles: Rol[]) => boolean }
const AuthContext = createContext<AuthValue | undefined>(undefined)
const StorageSchema = z.object({ token: z.string().min(1), expiraEn: z.number().positive() })
const CLAVE = 'pulsepass.sesion'
const CLAVE_AVISO = 'pulsepass.avisoSesion'

function leerSesionAlmacenada() {
  try {
    const resultado = StorageSchema.safeParse(JSON.parse(sessionStorage.getItem(CLAVE) ?? 'null'))
    if (resultado.success && resultado.data.expiraEn > Date.now()) return resultado.data
  } catch { /* la sesión corrupta se descarta */ }
  sessionStorage.removeItem(CLAVE)
  return undefined
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion>(() => {
    const almacenada = leerSesionAlmacenada()
    if (!almacenada) { establecerToken(null); return { estado: 'anonimo' } }
    establecerToken(almacenada.token)
    return { estado: 'cargando' }
  })
  const cerrarSesion = useCallback((expirada = false) => {
    establecerToken(null); sessionStorage.removeItem(CLAVE); queryClient.clear(); setSesion({ estado: 'anonimo' })
    if (expirada) { sessionStorage.setItem(CLAVE_AVISO, 'Tu sesión expiró. Inicia sesión de nuevo.'); window.location.assign('/login') }
  }, [])
  useEffect(() => { establecerManejador401(() => cerrarSesion(true)); return () => establecerManejador401(undefined) }, [cerrarSesion])
  useEffect(() => {
    const almacenada = leerSesionAlmacenada()
    if (!almacenada) return
    let vigente = true
    void sesionApi(almacenada.token).then((usuario) => { if (vigente) setSesion({ estado: 'autenticado', token: almacenada.token, expiraEn: almacenada.expiraEn, usuario }) }).catch(() => { if (vigente) cerrarSesion(false) })
    return () => { vigente = false }
  }, [cerrarSesion])
  useEffect(() => {
    if (sesion.estado !== 'autenticado') return
    const espera = Math.max(0, sesion.expiraEn - Date.now())
    const temporizador = window.setTimeout(() => cerrarSesion(true), espera)
    return () => window.clearTimeout(temporizador)
  }, [sesion, cerrarSesion])
  const iniciarSesion = useCallback(async (identifier: string, password: string) => {
    const respuesta = await loginApi(identifier, password)
    const expiraEn = Date.now() + respuesta.expiresIn * 1000
    sessionStorage.setItem(CLAVE, JSON.stringify({ token: respuesta.accessToken, expiraEn }))
    const usuario = await sesionApi(respuesta.accessToken)
    setSesion({ estado: 'autenticado', token: respuesta.accessToken, expiraEn, usuario })
  }, [])
  const valor = useMemo<AuthValue>(() => ({ sesion, iniciarSesion, cerrarSesion, tieneRol: (...roles) => sesion.estado === 'autenticado' && roles.some((rol) => sesion.usuario.roles.includes(rol)) }), [sesion, iniciarSesion, cerrarSesion])
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
export const useAuth = () => { const valor = useContext(AuthContext); if (!valor) throw new Error('useAuth debe utilizarse dentro de AuthProvider'); return valor }
export const consumirAvisoSesion = () => { const aviso = sessionStorage.getItem(CLAVE_AVISO); sessionStorage.removeItem(CLAVE_AVISO); return aviso ?? undefined }
