import { expect, test, vi } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Registro from './Registro'
import { renderConProveedores } from '../test/utils'

test('registro inválido no dispara una petición', async () => {
  const user = userEvent.setup()
  const fetchSpy = vi.spyOn(globalThis, 'fetch')
  renderConProveedores(<Registro/>)
  await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))
  expect(await screen.findByText('Ingresa un usuario')).toBeInTheDocument()
  expect(fetchSpy).not.toHaveBeenCalled()
  fetchSpy.mockRestore()
})
