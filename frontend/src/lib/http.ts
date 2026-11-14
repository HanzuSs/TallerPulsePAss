import type { ZodType } from 'zod'
import { ErrorResponseSchema } from '../funcionalidades/catalogo/tipos'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'
let tokenActual: string | null = null
let alNoAutorizado: (() => void) | undefined

export class ApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly details: Record<string, string> = {}) { super(message); this.name = 'ApiError' }
}

export function establecerToken(token: string | null) { tokenActual = token }
export function establecerManejador401(handler: (() => void) | undefined) { alNoAutorizado = handler }

export async function http<T>(ruta: string, schema: ZodType<T>, opciones: RequestInit & { omitir401?: boolean } = {}): Promise<T> {
  const headers = new Headers(opciones.headers)
  if (opciones.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (tokenActual) headers.set('Authorization', `Bearer ${tokenActual}`)
  try {
    const respuesta = await fetch(`${baseUrl}${ruta}`, { ...opciones, headers })
    const cuerpo: unknown = await respuesta.json().catch(() => undefined)
    if (!respuesta.ok) {
      const error = ErrorResponseSchema.safeParse(cuerpo)
      const apiError = error.success ? new ApiError(respuesta.status, error.data.message, error.data.details ?? {}) : new ApiError(respuesta.status, mensajeSeguro(respuesta.status))
      if (respuesta.status === 401 && !opciones.omitir401) alNoAutorizado?.()
      throw apiError
    }
    const dato = schema.safeParse(cuerpo)
    if (!dato.success) throw new ApiError(500, 'Recibimos una respuesta con un formato inesperado.')
    return dato.data
  } catch (error: unknown) {
    if (error instanceof ApiError) throw error
    throw new ApiError(0, 'No pudimos conectar con el servidor.')
  }
}

export const mensajeSeguro = (status: number) => {
  if (status === 400) return 'Revisa los datos ingresados.'
  if (status === 401) return 'Tu sesión expiró. Inicia sesión de nuevo.'
  if (status === 403) return 'No tienes permisos para realizar esta acción.'
  if (status === 404) return 'El recurso solicitado no está disponible.'
  if (status === 409) return 'La operación no se puede completar en este momento.'
  return 'Ocurrió un error inesperado. Inténtalo más tarde.'
}
