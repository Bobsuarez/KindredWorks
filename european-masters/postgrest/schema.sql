-- ============================================================
-- Masters Mailer - PostgreSQL Schema
-- ============================================================

CREATE SCHEMA IF NOT EXISTS public;

-- ------------------------------------------------------------
-- Table: areas
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS areas (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(150)    NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- Table: master_programs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS master_programs (
    id                   BIGSERIAL       PRIMARY KEY,
    name                 VARCHAR(300)    NOT NULL,
    area_id              BIGINT          NOT NULL,
    pdf_curriculum_path  VARCHAR(512)    NOT NULL,
    subject_image_path   VARCHAR(512)    NOT NULL,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_master_programs_area
        FOREIGN KEY (area_id) REFERENCES areas(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_master_programs_area_id ON master_programs(area_id);

-- ------------------------------------------------------------
-- Table: contacts
-- ------------------------------------------------------------
CREATE TYPE contact_status AS ENUM ('PENDING', 'SENT', 'ERROR');

CREATE TABLE IF NOT EXISTS contacts (
    id                BIGSERIAL       PRIMARY KEY,
    name              VARCHAR(200)    NOT NULL,
    email             VARCHAR(320)    NOT NULL,
    phone_number      VARCHAR(30)     NOT NULL,
    master_program_id BIGINT          NOT NULL,
    status            contact_status  NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_contacts_master_program
        FOREIGN KEY (master_program_id) REFERENCES master_programs(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contacts_master_program_id ON contacts(master_program_id);
CREATE INDEX IF NOT EXISTS idx_contacts_status ON contacts(status);
-- Composite index optimised for the scheduler query (PENDING ordered by id)
CREATE INDEX IF NOT EXISTS idx_contacts_status_id ON contacts(status, id);

-- ------------------------------------------------------------
-- Table: message_logs
-- ------------------------------------------------------------
CREATE TYPE delivery_status AS ENUM ('SUCCESS', 'FAILED');

CREATE TABLE IF NOT EXISTS message_logs (
    id                 BIGSERIAL        PRIMARY KEY,
    contact_id         BIGINT           NOT NULL,
    master_program_id  BIGINT           NOT NULL,
    email_status       delivery_status  NOT NULL,
    whatsapp_status    delivery_status  NOT NULL,
    email_sent_at      TIMESTAMPTZ,
    whatsapp_sent_at   TIMESTAMPTZ,
    error_message      TEXT,
    created_at         TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_message_logs_contact
        FOREIGN KEY (contact_id) REFERENCES contacts(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_message_logs_master_program
        FOREIGN KEY (master_program_id) REFERENCES master_programs(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_message_logs_contact_id      ON message_logs(contact_id);
CREATE INDEX IF NOT EXISTS idx_message_logs_master_program  ON message_logs(master_program_id);
CREATE INDEX IF NOT EXISTS idx_message_logs_created_at      ON message_logs(created_at);

-- ------------------------------------------------------------
-- Trigger: auto-update updated_at columns
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_areas_updated_at
    BEFORE UPDATE ON areas
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_master_programs_updated_at
    BEFORE UPDATE ON master_programs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_contacts_updated_at
    BEFORE UPDATE ON contacts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Seed data – academic areas
-- ------------------------------------------------------------
INSERT INTO areas (name) VALUES
    ('Comunicación'),
    ('Empresas'),
    ('Artes')
ON CONFLICT (name) DO NOTHING;
