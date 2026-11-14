import { z } from 'zod'
import { http } from '../../lib/http'
import { ArtistSchema, EventSchema, EventSummarySchema, VenueSchema } from './tipos'

const lista = <T>(schema: z.ZodType<T>) => z.array(schema)
export const obtenerEventosPublicados = () => http('/events/published', lista(EventSummarySchema))
export const obtenerEvento = (codigo: string) => http(`/events/${encodeURIComponent(codigo)}`, EventSchema)
export const obtenerEventosPorArtista = (nombre: string) => http(`/events/by-artist?stageName=${encodeURIComponent(nombre)}`, lista(EventSummarySchema))
export const obtenerArtistas = () => http('/artists/active', lista(ArtistSchema))
export const obtenerArtista = (id: number) => http(`/artists/${id}`, ArtistSchema)
export const obtenerArtistaPorNombre = (nombre: string) => http(`/artists/by-stage-name?stageName=${encodeURIComponent(nombre)}`, ArtistSchema)
export const obtenerSedes = () => http('/venues/active', lista(VenueSchema))
export const obtenerSede = (codigo: string) => http(`/venues/${encodeURIComponent(codigo)}`, VenueSchema)

export type CrearEvento = { eventCode: string; name: string; description?: string; category: string; eventDate: string; minimumAge: unknown; venueCode: string }
export const crearEvento = (datos: CrearEvento) => http('/events', EventSchema, { method: 'POST', body: JSON.stringify({ ...datos, minimumAge: Number(datos.minimumAge) }) })
export const publicarEvento = (codigo: string) => http(`/events/${encodeURIComponent(codigo)}/publish`, EventSchema, { method: 'PATCH' })
export const asociarArtista = (codigo: string, artistaId: number) => http(`/events/${encodeURIComponent(codigo)}/artists/${artistaId}`, EventSchema, { method: 'POST' })
