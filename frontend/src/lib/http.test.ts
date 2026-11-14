import { expect, test } from 'vitest'
import { http as mswHttp, HttpResponse } from 'msw'
import { z } from 'zod'
import { establecerToken, http } from './http'
import { server } from '../test/setup'

test('envía el token exclusivamente en Authorization', async () => {
  establecerToken('token-de-prueba')
  server.use(mswHttp.get('/api/protegido', ({ request }) => {
    expect(request.headers.get('Authorization')).toBe('Bearer token-de-prueba')
    expect(new URL(request.url).searchParams.has('token')).toBe(false)
    return HttpResponse.json({ ok: true })
  }))
  await expect(http('/protegido', z.object({ ok: z.literal(true) }))).resolves.toEqual({ ok: true })
  establecerToken(null)
})

test('convierte ErrorResponse en ApiError con detalles', async () => {
  server.use(mswHttp.post('/api/registro', () => HttpResponse.json({ status: 400, message: 'Email inválido', details: { email: 'Formato inválido' } }, { status: 400 })))
  await expect(http('/registro', z.object({ ok: z.boolean() }), { method: 'POST' })).rejects.toEqual(expect.objectContaining({ status: 400, message: 'Email inválido', details: { email: 'Formato inválido' } }))
})

test('normaliza cuerpos de error inválidos', async () => {
  server.use(mswHttp.get('/api/caido', () => HttpResponse.text('fallo', { status: 500 })))
  await expect(http('/caido', z.object({}))).rejects.toEqual(expect.objectContaining({ status: 500, message: 'Ocurrió un error inesperado. Inténtalo más tarde.' }))
})
