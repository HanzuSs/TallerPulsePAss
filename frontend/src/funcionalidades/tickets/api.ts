import { z } from 'zod'
import { http } from '../../lib/http'
import { TicketSchema, type TicketType } from '../catalogo/tipos'

const tickets = z.array(TicketSchema)
export const obtenerTicket = (codigo: string) => http(`/tickets/${encodeURIComponent(codigo)}`, TicketSchema)
export const obtenerTicketsUsuario = (email: string) => http(`/tickets/by-user?email=${encodeURIComponent(email)}`, tickets)
export const obtenerTicketsPagados = (evento: string) => http(`/events/${encodeURIComponent(evento)}/tickets/paid`, tickets)
export const comprarTicket = (datos: { userEmail: string; eventCode: string; type: TicketType }) => http('/tickets', TicketSchema, { method: 'POST', body: JSON.stringify(datos) })
export const cancelarTicket = (codigo: string) => http(`/tickets/${encodeURIComponent(codigo)}/cancel`, TicketSchema, { method: 'PATCH' })
export const usarTicket = (codigo: string) => http(`/tickets/${encodeURIComponent(codigo)}/use`, TicketSchema, { method: 'PATCH' })
