-- Datos de prueba locales para visualizar el catálogo de PulsePass.

INSERT INTO venues (code, name, city, address, capacity, active) VALUES
                                                                     ('PP-BOG-ARENA', 'Pulse Arena Bogotá', 'Bogotá',
                                                                      'Carrera 30 # 45-03', 12000, TRUE),
                                                                     ('PP-MED-TEATRO', 'Teatro Pulse Medellín', 'Medellín',
                                                                      'Calle 10 # 43A-20', 2500, TRUE)
    ON CONFLICT (code) DO NOTHING;


INSERT INTO events (
    event_code,
    name,
    description,
    category,
    status,
    event_date,
    minimum_age,
    venue_id,
    streaming_url
)
SELECT
    'EVT-SOLAR-2026',
    'Solar Beat: Noche Electrónica',
    'Una noche de música electrónica con artistas latinoamericanos.',
    'MUSIC',
    'PUBLISHED',
    TIMESTAMP '2026-12-12 20:00:00',
    18,
    v.id,
    NULL
FROM venues v
WHERE v.code = 'PP-BOG-ARENA'
    ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (
    event_code,
    name,
    description,
    category,
    status,
    event_date,
    minimum_age,
    venue_id,
    streaming_url
)
SELECT
    'EVT-TECH-2027',
    'Pulse Tech Summit 2027',
    'Conferencias, talleres y experiencias sobre tecnología.',
    'TECHNOLOGY',
    'PUBLISHED',
    TIMESTAMP '2027-01-23 09:00:00',
    14,
    v.id,
    'https://www.youtube.com'
FROM venues v
WHERE v.code = 'PP-MED-TEATRO'
    ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (
    event_code,
    name,
    description,
    category,
    status,
    event_date,
    minimum_age,
    venue_id,
    streaming_url
)
SELECT
    'EVT-CARIBBEAN-2027',
    'Festival Caribbean Sound',
    'Festival tropical con artistas invitados y gastronomía.',
    'ENTERTAINMENT',
    'PUBLISHED',
    TIMESTAMP '2027-02-14 16:00:00',
    0,
    v.id,
    NULL
FROM venues v
WHERE v.code = 'PP-BOG-ARENA'
    ON CONFLICT (event_code) DO NOTHING;


INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
         JOIN artists a ON a.stage_name = 'Solar Beat'
WHERE e.event_code = 'EVT-SOLAR-2026'
    ON CONFLICT (event_id, artist_id) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
         JOIN artists a ON a.stage_name = 'Digital Pulse'
WHERE e.event_code = 'EVT-SOLAR-2026'
    ON CONFLICT (event_id, artist_id) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
         JOIN artists a ON a.stage_name = 'Neon Waves'
WHERE e.event_code = 'EVT-TECH-2027'
    ON CONFLICT (event_id, artist_id) DO NOTHING;

INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
         JOIN artists a ON a.stage_name = 'Caribbean Sound'
WHERE e.event_code = 'EVT-CARIBBEAN-2027'
    ON CONFLICT (event_id, artist_id) DO NOTHING;