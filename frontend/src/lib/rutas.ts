export const RUTAS = {
  inicio: '/', eventos: '/eventos', evento: (codigo: string) => `/eventos/${encodeURIComponent(codigo)}`,
  artistas: '/artistas', artista: (id: number | string) => `/artistas/${id}`,
  sedes: '/sedes', sede: (codigo: string) => `/sedes/${encodeURIComponent(codigo)}`,
  login: '/login', registro: '/registro', cuenta: '/cuenta', misTickets: '/mis-tickets',
  ticket: (codigo: string) => `/mis-tickets/${encodeURIComponent(codigo)}`,
  confirmacion: (codigo: string) => `/compra/confirmacion/${encodeURIComponent(codigo)}`,
  validacion: '/staff/validacion', ticketsPagados: (codigo: string) => `/staff/eventos/${encodeURIComponent(codigo)}/tickets`,
  adminEventos: '/admin/eventos', adminNuevo: '/admin/eventos/nuevo',
  adminGestion: (codigo: string) => `/admin/eventos/${encodeURIComponent(codigo)}`,
  sinPermisos: '/403',
} as const
