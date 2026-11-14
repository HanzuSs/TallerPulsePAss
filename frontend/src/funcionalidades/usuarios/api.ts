import { http } from '../../lib/http'
import { UserSchema } from '../catalogo/tipos'

export type Registro = { username: string; email: string; firstName: string; lastName: string; phone?: string; city?: string; birthDate: string; password: string }
export const registrarUsuario = (datos: Registro) => http('/users', UserSchema, { method: 'POST', body: JSON.stringify(datos), omitir401: true })
export const obtenerUsuarioPorEmail = (email: string) => http(`/users/by-email?email=${encodeURIComponent(email)}`, UserSchema)
export const obtenerUsuarioPorUsername = (username: string) => http(`/users/by-username?username=${encodeURIComponent(username)}`, UserSchema)
