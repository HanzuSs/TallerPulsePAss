import { expect, test } from 'vitest'
import { screen } from '@testing-library/react'
import CatalogoEventos from './CatalogoEventos'
import { renderConProveedores } from '../test/utils'

test('muestra un estado vacío cuando no hay eventos', async () => {
  renderConProveedores(<CatalogoEventos/>, '/eventos')
  expect(await screen.findByText('Sin resultados para tu búsqueda')).toBeInTheDocument()
})
