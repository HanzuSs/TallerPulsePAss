import { useMutation, useQuery } from '@tanstack/react-query'
import { obtenerUsuarioPorEmail, obtenerUsuarioPorUsername, registrarUsuario, type Registro } from './api'
export const useUsuarioPorEmail = (email: string | undefined) => useQuery({ queryKey: ['usuario', email], queryFn: () => obtenerUsuarioPorEmail(email as string), enabled: Boolean(email), staleTime: 60_000 })
export const useUsuarioPorUsername = (username: string | undefined) => useQuery({ queryKey: ['usuario', 'username', username], queryFn: () => obtenerUsuarioPorUsername(username as string), enabled: Boolean(username), staleTime: 60_000 })
export const useRegistrarUsuario = () => useMutation({ mutationFn: (datos: Registro) => registrarUsuario(datos) })
