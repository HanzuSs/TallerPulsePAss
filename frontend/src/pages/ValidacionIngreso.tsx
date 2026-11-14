import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ApiError } from '../lib/http'
import { Aviso, Badge, Boton, Cargando, EstadoError, Tarjeta } from '../componentes/ui'
import { useTicket, useUsarTicket } from '../funcionalidades/tickets/hooks'

const Schema = z.object({ codigo: z.string().min(1, 'Ingresa un código de ticket') })
type Form = z.infer<typeof Schema>

export default function ValidacionIngreso() {
  const [busqueda, setBusqueda] = useState('')
  const [aviso, setAviso] = useState<{ tipo: 'exito' | 'error'; mensaje: string }>()
  const campo = useRef<HTMLInputElement>(null)
  const formulario = useForm<Form>({ resolver: zodResolver(Schema) })
  const registro = formulario.register('codigo')
  const ticket = useTicket(busqueda)
  const usar = useUsarTicket()
  useEffect(() => campo.current?.focus(), [])
  const buscar = formulario.handleSubmit(({ codigo }) => { setAviso(undefined); setBusqueda(codigo.trim()) })
  const marcar = async () => {
    if (!ticket.data) return
    setAviso(undefined)
    try { await usar.mutateAsync(ticket.data.ticketCode); setAviso({ tipo: 'exito', mensaje: 'Ingreso registrado.' }); formulario.reset(); setBusqueda(''); campo.current?.focus() }
    catch (fallo: unknown) { setAviso({ tipo: 'error', mensaje: fallo instanceof ApiError ? fallo.message : 'No pudimos registrar el ingreso.' }) }
  }
  return <div className="mx-auto max-w-3xl"><h1 className="font-titulo text-3xl font-bold">Validación de ingreso</h1><form className="mt-6 flex flex-col gap-3 sm:flex-row" onSubmit={buscar} noValidate><div className="flex-1"><label className="sr-only" htmlFor="codigo">Código de ticket</label><input id="codigo" className="min-h-12 w-full rounded-lg border border-slate-300 bg-white px-3 dark:border-slate-600 dark:bg-slate-900" placeholder="TKT-8F2A1C" {...registro} ref={(elemento) => { registro.ref(elemento); campo.current = elemento }}/>{formulario.formState.errors.codigo && <span className="mt-1 block text-sm text-red-600">{formulario.formState.errors.codigo.message}</span>}</div><Boton type="submit">Buscar</Boton></form>{busqueda && ticket.isPending && <div className="mt-6"><Cargando/></div>}{busqueda && ticket.isError && <div className="mt-6"><EstadoError mensaje={ticket.error instanceof ApiError && ticket.error.status === 404 ? 'Ticket no encontrado' : 'No pudimos consultar el ticket.'} reintentar={() => void ticket.refetch()}/></div>}{ticket.data && <Tarjeta className="mt-6"><div className="flex flex-wrap items-start justify-between gap-4"><div><h2 className="font-titulo text-xl font-bold">{ticket.data.eventName}</h2><p className="mt-2">{ticket.data.type} · Titular: {ticket.data.userEmail}</p></div><Badge estado={ticket.data.status}/></div>{ticket.data.status === 'PAID' && <Boton className="mt-5" onClick={() => void marcar()} cargando={usar.isPending}>Marcar como usado</Boton>}</Tarjeta>}{aviso && <div className="mt-5"><Aviso tipo={aviso.tipo}>{aviso.mensaje}</Aviso></div>}</div>
}
