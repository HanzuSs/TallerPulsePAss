import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/events/published', () => HttpResponse.json([])),
  http.get('/api/artists/active', () => HttpResponse.json([])),
  http.get('/api/venues/active', () => HttpResponse.json([])),
]
