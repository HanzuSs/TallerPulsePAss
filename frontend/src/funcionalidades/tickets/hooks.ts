import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { cancelarTicket, comprarTicket, obtenerTicket, obtenerTicketsPagados, obtenerTicketsUsuario, usarTicket } from './api'
import type { TicketType } from '../catalogo/tipos'

export const useTicket = (codigo: string) => useQuery({ queryKey: ['ticket', codigo], queryFn: () => obtenerTicket(codigo), enabled: Boolean(codigo), staleTime: 15_000 })
export const useMisTickets = (email: string | undefined) => useQuery({ queryKey: ['tickets', 'usuario', email], queryFn: () => obtenerTicketsUsuario(email as string), enabled: Boolean(email), staleTime: 15_000 })
export const useTicketsPagados = (codigo: string) => useQuery({ queryKey: ['tickets', 'pagados', codigo], queryFn: () => obtenerTicketsPagados(codigo), enabled: Boolean(codigo), staleTime: 15_000 })
export const useComprarTicket = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: (datos: { userEmail: string; eventCode: string; type: TicketType }) => comprarTicket(datos), onSuccess: (ticket) => { void cliente.invalidateQueries({ queryKey: ['tickets', 'usuario', ticket.userEmail] }); void cliente.invalidateQueries({ queryKey: ['evento', ticket.eventCode] }) } }) }
const invalidarTicket = (cliente: ReturnType<typeof useQueryClient>, ticket: { ticketCode: string; userEmail: string; eventCode: string }) => { void cliente.invalidateQueries({ queryKey: ['ticket', ticket.ticketCode] }); void cliente.invalidateQueries({ queryKey: ['tickets', 'usuario', ticket.userEmail] }); void cliente.invalidateQueries({ queryKey: ['tickets', 'pagados', ticket.eventCode] }) }
export const useCancelarTicket = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: cancelarTicket, onSuccess: (ticket) => invalidarTicket(cliente, ticket) }) }
export const useUsarTicket = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: usarTicket, onSuccess: (ticket) => invalidarTicket(cliente, ticket) }) }
