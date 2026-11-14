import { Link } from 'react-router'
import { Tarjeta } from '../componentes/ui'
import { RUTAS } from '../lib/rutas'
export default function SinPermisos() { return <Tarjeta className="mx-auto max-w-xl py-14 text-center"><p className="font-titulo text-6xl font-black text-marca-600">403</p><h1 className="mt-4 font-titulo text-2xl font-bold">No tienes permisos para acceder a esta sección.</h1><Link className="mt-7 inline-block rounded-lg bg-marca-600 px-4 py-3 font-semibold text-white" to={RUTAS.inicio}>Volver al inicio</Link></Tarjeta> }
