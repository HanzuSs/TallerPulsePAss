import { expect, test, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Login from './Login'
import AdminCrearEvento from './AdminCrearEvento'
import ValidacionIngreso from './ValidacionIngreso'
import { renderConAuth, renderConProveedores } from '../test/utils'

test('login vacío muestra errores sin consultar la API', async () => {
  const user = userEvent.setup(); const fetchSpy = vi.spyOn(globalThis, 'fetch')
  renderConAuth(<Login/>)
  await user.click(screen.getByRole('button', { name: 'Ingresar' }))
  expect(await screen.findByText('Ingresa tu usuario o correo')).toBeInTheDocument()
  expect(fetchSpy).not.toHaveBeenCalled(); fetchSpy.mockRestore()
})

test('validación de ingreso exige un código', async () => {
  const user = userEvent.setup()
  renderConProveedores(<ValidacionIngreso/>)
  await user.click(screen.getByRole('button', { name: 'Buscar' }))
  expect(await screen.findByText('Ingresa un código de ticket')).toBeInTheDocument()
})

test('crear evento vacío no envía una mutación', async () => {
  const user = userEvent.setup(); const fetchSpy = vi.spyOn(globalThis, 'fetch')
  renderConProveedores(<AdminCrearEvento/>)
  await screen.findByRole('button', { name: 'Crear evento' })
  fetchSpy.mockClear()
  await user.click(screen.getByRole('button', { name: 'Crear evento' }))
  expect(await screen.findByText('Ingresa el código')).toBeInTheDocument()
  expect(fetchSpy).not.toHaveBeenCalled(); fetchSpy.mockRestore()
})
