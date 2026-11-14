import { Link } from 'react-router'
import { Boton, Cargando, EstadoError, Tarjeta } from '../componentes/ui'
import { RUTAS } from '../lib/rutas'
import { FechaCorta } from '../lib/formato'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useUsuarioPorEmail, useUsuarioPorUsername } from '../funcionalidades/usuarios/hooks'

export default function Cuenta() {
  const { sesion, cerrarSesion } = useAuth()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : undefined
  const username = sesion.estado === 'autenticado' ? sesion.usuario.username : undefined
  const usuario = useUsuarioPorEmail(email)
  const usuarioPorNombre = useUsuarioPorUsername(username)
  if (usuario.isPending || usuarioPorNombre.isPending) return <Cargando className="h-56"/>
  if (usuario.isError || usuarioPorNombre.isError) return <EstadoError mensaje="No pudimos cargar tu cuenta." reintentar={() => { void usuario.refetch(); void usuarioPorNombre.refetch() }}/>
  const datos = usuario.data
  return <div><h1 className="font-titulo text-3xl font-bold">Mi cuenta</h1><Tarjeta className="mt-6"><div className="flex flex-col gap-4 sm:flex-row sm:items-center"><div className="grid h-16 w-16 place-items-center rounded-full bg-marca-100 font-titulo text-2xl font-bold text-marca-700">{datos.firstName.slice(0, 1)}</div><div><h2 className="font-titulo text-xl font-bold">{datos.firstName} {datos.lastName}</h2><p className="text-slate-600 dark:text-slate-400">@{datos.username} · {datos.email}</p></div><div className="sm:ml-auto"><span className="rounded-full bg-marca-50 px-3 py-1 text-sm font-bold text-marca-700">{sesion.estado === 'autenticado' ? sesion.usuario.roles.join(', ') : ''}</span></div></div><dl className="mt-7 grid gap-4 border-t border-slate-200 pt-5 sm:grid-cols-3 dark:border-slate-700"><div><dt className="text-sm text-slate-500">Nacimiento</dt><dd>{FechaCorta(datos.birthDate)}</dd></div><div><dt className="text-sm text-slate-500">Estado</dt><dd>{datos.active ? 'Activa' : 'Inactiva'}</dd></div><div><dt className="text-sm text-slate-500">Ciudad</dt><dd>{datos.city || 'No registrada'}</dd></div></dl></Tarjeta><div className="mt-6 flex flex-wrap justify-end gap-3"><Link className="inline-flex min-h-11 items-center justify-center rounded-lg border border-slate-300 px-4 font-medium dark:border-slate-600" to={RUTAS.misTickets}>Ver mis tickets</Link><Boton variante="peligro" onClick={() => cerrarSesion()}>Cerrar sesión</Boton></div></div>
}
