import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import type { ReactElement } from 'react'
import { AuthProvider } from '../funcionalidades/auth/AuthContext'

export function renderConProveedores(ui: ReactElement, ruta = '/') {
  const cliente = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(<QueryClientProvider client={cliente}><MemoryRouter initialEntries={[ruta]}>{ui}</MemoryRouter></QueryClientProvider>)
}

export function renderConAuth(ui: ReactElement, ruta = '/') {
  const cliente = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(<QueryClientProvider client={cliente}><AuthProvider><MemoryRouter initialEntries={[ruta]}>{ui}</MemoryRouter></AuthProvider></QueryClientProvider>)
}
