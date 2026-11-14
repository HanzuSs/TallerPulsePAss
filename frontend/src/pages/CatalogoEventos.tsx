import { useMemo } from 'react'
import { Link, useSearchParams } from 'react-router'
import { Cargando, EstadoError, EstadoVacio, Selector } from '../componentes/ui'
import { TarjetaEvento } from '../componentes/tarjetas'
import { RUTAS } from '../lib/rutas'
import { EventCategorySchema } from '../funcionalidades/catalogo/tipos'
import { useArtistaPorNombre, useEventosPorArtista, useEventosPublicados } from '../funcionalidades/catalogo/hooks'

export default function CatalogoEventos() {
  const [parametros, setParametros] = useSearchParams()
  const texto = parametros.get('q') ?? ''
  const categoria = parametros.get('categoria') ?? ''
  const artista = parametros.get('artista') ?? ''
  const publicados = useEventosPublicados()
  const porArtista = useEventosPorArtista(artista)
  const artistaEncontrado = useArtistaPorNombre(artista)
  const consulta = artista ? porArtista : publicados
  const eventos = useMemo(() => (consulta.data ?? []).filter((evento) => evento.name.toLocaleLowerCase().includes(texto.toLocaleLowerCase()) && (!categoria || evento.category === categoria)), [consulta.data, texto, categoria])
  const actualizar = (clave: string, valor: string) => {
    const siguiente = new URLSearchParams(parametros)
    if (valor) siguiente.set(clave, valor)
    else siguiente.delete(clave)
    setParametros(siguiente, { replace: true })
  }
  return <div><h1 className="font-titulo text-3xl font-bold">Cartelera</h1><div className="mt-6 grid gap-3 rounded-xl bg-white p-4 shadow-sm sm:grid-cols-3 dark:bg-slate-900"><label className="block sm:col-span-2"><span className="sr-only">Buscar por nombre</span><input value={texto} onChange={(event) => actualizar('q', event.target.value)} placeholder="Buscar por nombre…" className="min-h-11 w-full rounded-lg border border-slate-300 bg-white px-3 dark:border-slate-600 dark:bg-slate-950"/></label><Selector etiqueta="Categoría" value={categoria} onChange={(event) => actualizar('categoria', event.target.value)}><option value="">Todas las categorías</option>{EventCategorySchema.options.map((valor) => <option key={valor}>{valor}</option>)}</Selector><label className="block"><span className="sr-only">Artista</span><input value={artista} onChange={(event) => actualizar('artista', event.target.value)} placeholder="Artista (nombre artístico)" className="min-h-11 w-full rounded-lg border border-slate-300 bg-white px-3 dark:border-slate-600 dark:bg-slate-950"/></label><button type="button" className="min-h-11 rounded-lg border border-slate-300 px-4 font-medium hover:bg-slate-50 dark:border-slate-600 dark:hover:bg-slate-800" onClick={() => setParametros({})}>Limpiar filtros</button></div>{artista && artistaEncontrado.data && <p className="mt-3 text-sm">Perfil: <Link className="font-semibold text-marca-700 hover:underline" to={RUTAS.artista(artistaEncontrado.data.id)}>{artistaEncontrado.data.stageName}</Link></p>}{consulta.isPending ? <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3"><Cargando/><Cargando/><Cargando/></div> : consulta.isError ? <div className="mt-6"><EstadoError mensaje="No pudimos cargar la cartelera." reintentar={() => void consulta.refetch()}/></div> : <><p className="mt-6 text-sm text-slate-600 dark:text-slate-400">{eventos.length} eventos</p>{eventos.length === 0 ? <div className="mt-6"><EstadoVacio mensaje="Sin resultados para tu búsqueda"/></div> : <div className="mt-4 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{eventos.map((evento) => <TarjetaEvento key={evento.eventCode} evento={evento}/>)}</div>}</>}</div>
}
