import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { Aviso, Boton, Campo, Tarjeta } from '../componentes/ui'
import { consumirAvisoSesion, useAuth } from '../funcionalidades/auth/AuthContext'

const LoginSchema = z.object({ identifier: z.string().min(1, 'Ingresa tu usuario o correo'), password: z.string().min(1, 'Ingresa tu contraseña') })
type LoginForm = z.infer<typeof LoginSchema>
const destino = (state: unknown) => z.object({ desde: z.string().startsWith('/') }).safeParse(state).data?.desde ?? RUTAS.inicio
export default function Login() { const { iniciarSesion } = useAuth(); const navegar = useNavigate(); const ubicacion = useLocation(); const [error, setError] = useState<string | undefined>(() => consumirAvisoSesion()); const formulario = useForm<LoginForm>({ resolver: zodResolver(LoginSchema) }); const enviar = formulario.handleSubmit(async (datos) => { setError(undefined); try { await iniciarSesion(datos.identifier, datos.password); navegar(destino(ubicacion.state), { replace: true }) } catch (fallo: unknown) { setError(fallo instanceof ApiError && fallo.status === 401 ? 'Credenciales inválidas.' : 'Inténtalo más tarde.') } }); return <div className="mx-auto max-w-md"><Tarjeta><div className="text-center"><p className="font-titulo text-xl font-bold text-marca-700">PulsePass</p><h1 className="mt-2 font-titulo text-3xl font-bold">Inicia sesión</h1></div><form className="mt-7 space-y-4" onSubmit={enviar} noValidate><Campo etiqueta="Usuario o correo" autoComplete="username" error={formulario.formState.errors.identifier?.message} {...formulario.register('identifier')}/><Campo etiqueta="Contraseña" type="password" autoComplete="current-password" error={formulario.formState.errors.password?.message} {...formulario.register('password')}/>{error && <Aviso tipo="error">{error}</Aviso>}<Boton className="w-full" type="submit" cargando={formulario.formState.isSubmitting}>Ingresar</Boton></form><p className="mt-6 text-center text-sm">¿No tienes cuenta? <Link className="font-semibold text-marca-700 hover:underline" to={RUTAS.registro}>Regístrate</Link></p></Tarjeta></div> }
