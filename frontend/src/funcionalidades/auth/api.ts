import { AuthResponseSchema, AuthenticatedUserSchema } from '../catalogo/tipos'
import { establecerToken, http } from '../../lib/http'

export const loginApi = (identifier: string, password: string) => http('/auth/login', AuthResponseSchema, { method: 'POST', body: JSON.stringify({ identifier, password }), omitir401: true })
export const sesionApi = (token: string) => { establecerToken(token); return http('/auth/me', AuthenticatedUserSchema) }
