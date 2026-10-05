-- =====================================================================
-- PulsePass V3 - Evento hibrido (FR-EVT-006)
-- Se agrega la columna en una migracion nueva; V1 permanece intacta.
-- =====================================================================

ALTER TABLE events
    ADD COLUMN streaming_url VARCHAR(500);
