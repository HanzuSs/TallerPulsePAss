import { expect, test } from 'vitest'
import { screen } from '@testing-library/react'
import NoEncontrada from './NoEncontrada'
import SinPermisos from './SinPermisos'
import { renderConProveedores } from '../test/utils'

test('la página 404 comunica una ruta inexistente', () => { renderConProveedores(<NoEncontrada/>); expect(screen.getByRole('heading', { name: 'No encontramos esta página' })).toBeInTheDocument() })
test('la página 403 comunica permisos insuficientes', () => { renderConProveedores(<SinPermisos/>); expect(screen.getByRole('heading', { name: 'No tienes permisos para acceder a esta sección.' })).toBeInTheDocument() })
