import { Cargando, EstadoError, EstadoVacio } from '../componentes/ui'
import { AvatarArtista } from '../componentes/tarjetas'
import { useArtistasActivos } from '../funcionalidades/catalogo/hooks'

export default function ListaArtistas() { const artistas = useArtistasActivos(); return <div><h1 className="font-titulo text-3xl font-bold">Artistas</h1>{artistas.isPending ? <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4"><Cargando/><Cargando/><Cargando/><Cargando/></div> : artistas.isError ? <div className="mt-6"><EstadoError mensaje="No pudimos cargar los artistas." reintentar={() => void artistas.refetch()}/></div> : artistas.data.length === 0 ? <div className="mt-6"><EstadoVacio mensaje="Aún no hay artistas activos"/></div> : <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">{artistas.data.map((artista) => <AvatarArtista key={artista.id} artista={artista}/>)}</div>}</div> }
