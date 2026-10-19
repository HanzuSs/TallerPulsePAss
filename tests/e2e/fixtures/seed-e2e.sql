-- =====================================================================
-- seed-e2e.sql  -  EXCLUSIVO para la BD de pruebas pulsepass_e2e
-- Aplicar DESPUES de las migraciones Flyway (V1, V2, V3 y las de Fases 3-4).
-- Los codigos coinciden con los valores por defecto de
-- PulsePass-local.postman_environment.json (runId = r001).
-- =====================================================================

-- Guardia: aborta si no se esta en la base E2E.
DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'seed-e2e.sql solo puede ejecutarse en pulsepass_e2e (actual: %)', current_database();
    END IF;
END $$;

-- Venues (la Fase 3 solo expone GET; los venues se preprovisionan)
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
    ('VEN-E2E-001', 'Venue E2E Activo', 'Bogota', 'Calle 1 # 2-3', 500, TRUE),
    ('VEN-E2E-INACTIVO', 'Venue E2E Inactivo', 'Bogota', 'Calle 4 # 5-6', 100, FALSE);

-- Eventos. AJUSTAR event_date si ya es pasada (deben ser futuras).
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT e.code, e.name, 'Fixture E2E', 'MUSIC', e.status, TIMESTAMP '2027-12-15 20:00:00', e.min_age, v.id
FROM (VALUES
    ('EVT-E2E-PUB-r001',    'E2E Publicado r001',      'PUBLISHED', 0),
    ('EVT-E2E-DRAFT-r001',  'E2E Borrador r001',       'DRAFT',     0),
    ('EVT-E2E-TOPUB-r001',  'E2E Por publicar r001',   'DRAFT',     0),
    ('EVT-E2E-AGE-r001',    'E2E Edad minima r001',    'PUBLISHED', 60),
    ('EVT-E2E-SOLD-r001',   'E2E Agotado r001',        'SOLD_OUT',  0)
) AS e(code, name, status, min_age)
CROSS JOIN (SELECT id FROM venues WHERE code = 'VEN-E2E-001') v;

-- ---------------------------------------------------------------------
-- STAFF, ADMIN e INACTIVE_USER
-- NO incluidos: dependen del esquema de la Fase 4 (columna de password
-- hash BCrypt y de rol), que NO esta en el codigo entregado. Completar
-- con las columnas reales, por ejemplo (PLANTILLA, no ejecutable):
--
--   INSERT INTO users (username, email, active, <password_col>, <role_col>)
--   VALUES ('staff_e2e', 'staff.e2e@example.test', TRUE, '<BCRYPT_HASH>', 'STAFF');
--
-- Generar el hash BCrypt fuera del repositorio; nunca commitear passwords.
-- ---------------------------------------------------------------------
