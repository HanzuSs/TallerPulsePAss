import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { asociarArtista, crearEvento, obtenerArtista, obtenerArtistaPorNombre, obtenerArtistas, obtenerEvento, obtenerEventosPorArtista, obtenerEventosPublicados, obtenerSede, obtenerSedes, publicarEvento, type CrearEvento } from './api'

export const useEventosPublicados = () => useQuery({ queryKey: ['eventos', 'publicados'], queryFn: obtenerEventosPublicados, staleTime: 60_000 })
export const useEvento = (codigo: string) => useQuery({ queryKey: ['evento', codigo], queryFn: () => obtenerEvento(codigo), enabled: Boolean(codigo), staleTime: 30_000 })
export const useEventosPorArtista = (nombre: string) => useQuery({ queryKey: ['eventos', 'artista', nombre], queryFn: () => obtenerEventosPorArtista(nombre), enabled: Boolean(nombre), staleTime: 60_000 })
export const useArtistasActivos = () => useQuery({ queryKey: ['artistas', 'activos'], queryFn: obtenerArtistas, staleTime: 300_000 })
export const useArtista = (id: number | undefined) => useQuery({ queryKey: ['artista', id], queryFn: () => obtenerArtista(id as number), enabled: Number.isInteger(id) && (id ?? 0) > 0, staleTime: 300_000 })
export const useArtistaPorNombre = (nombre: string) => useQuery({ queryKey: ['artista', 'nombre', nombre], queryFn: () => obtenerArtistaPorNombre(nombre), enabled: Boolean(nombre) })
export const useSedesActivas = () => useQuery({ queryKey: ['sedes', 'activas'], queryFn: obtenerSedes, staleTime: 300_000 })
export const useSede = (codigo: string) => useQuery({ queryKey: ['sede', codigo], queryFn: () => obtenerSede(codigo), enabled: Boolean(codigo), staleTime: 300_000 })

export const useCrearEvento = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: (datos: CrearEvento) => crearEvento(datos), onSuccess: (evento) => cliente.setQueryData(['evento', evento.eventCode], evento) }) }
export const usePublicarEvento = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: publicarEvento, onSuccess: (evento) => { cliente.setQueryData(['evento', evento.eventCode], evento); void cliente.invalidateQueries({ queryKey: ['eventos', 'publicados'] }) } }) }
export const useAsociarArtista = () => { const cliente = useQueryClient(); return useMutation({ mutationFn: ({ codigo, artistaId }: { codigo: string; artistaId: number }) => asociarArtista(codigo, artistaId), onSuccess: (evento) => { cliente.setQueryData(['evento', evento.eventCode], evento); void cliente.invalidateQueries({ queryKey: ['eventos', 'artista'] }) } }) }
