-- ============================================================
-- Masters Mailer - PostgreSQL Schema
-- ============================================================

CREATE SCHEMA IF NOT EXISTS public;

-- ------------------------------------------------------------
-- Table: master_programs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS master_programs (
    id                   BIGSERIAL       PRIMARY KEY,
    name                 VARCHAR(300)    NOT NULL UNIQUE,
    pdf_curriculum_path  VARCHAR(512)    NULL,
    subject_image_path   VARCHAR(512)    NULL,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_master_programs_name ON master_programs(name);

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- ------------------------------------------------------------
-- Table: import_jobs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS import_jobs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename      TEXT NOT NULL,
    original_name TEXT,
    status        TEXT DEFAULT 'PENDING',
    total_rows    INT,
    processed     INT DEFAULT 0,
    errors        INT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT NOW(),
    finished_at   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_import_jobs_status ON import_jobs(status);
CREATE INDEX IF NOT EXISTS idx_import_jobs_created_at ON import_jobs(created_at);
CREATE INDEX IF NOT EXISTS idx_import_jobs_finished_at ON import_jobs(finished_at);

-- ------------------------------------------------------------
-- Table: contacts
-- ------------------------------------------------------------
-- Limpieza: quitar tablas que usan contact_status y el tipo enum (status debe ser VARCHAR por JPA)
DROP TABLE IF EXISTS message_logs;
DROP TABLE IF EXISTS contacts;
DROP TYPE IF EXISTS contact_status;


CREATE TYPE contact_status AS ENUM ('PENDING', 'SENT', 'ERROR');

CREATE TABLE contacts (
    id                BIGSERIAL       PRIMARY KEY,
    name              VARCHAR(200)    NOT NULL,
    email             VARCHAR(320)    NOT NULL UNIQUE,
    phone_number      VARCHAR(30)     NOT NULL,
    country           VARCHAR(50)     NOT NULL DEFAULT 'Desconocido',
    master_program_id BIGINT          NOT NULL,
    status            contact_status  NOT NULL DEFAULT 'PENDING'::contact_status,
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

CREATE TABLE IF NOT EXISTS message_logs (
    id                 BIGSERIAL        PRIMARY KEY,
    contact_id         BIGINT           NOT NULL,
    master_program_id  BIGINT           NOT NULL,
    email_status       VARCHAR(30)      NOT NULL,
    whatsapp_status    VARCHAR(30)      NOT NULL,
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
-- Table: program_notification_settings
-- ------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'notification_channel') THEN
        CREATE TYPE notification_channel AS ENUM ('EMAIL', 'WHATSAPP');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS program_notification_settings (
    id                 BIGSERIAL            PRIMARY KEY,
    master_program_id  BIGINT               NOT NULL,
    channel            notification_channel NOT NULL,
    is_enabled         BOOLEAN              NOT NULL DEFAULT TRUE,
    effective_from     TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    effective_to       TIMESTAMPTZ          NULL,
    updated_by         VARCHAR(150)         NULL,
    reason             VARCHAR(300)         NULL,
    created_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_pns_master_program
        FOREIGN KEY (master_program_id) REFERENCES master_programs(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_pns_effective_range
        CHECK (effective_to IS NULL OR effective_to > effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_pns_active_program_channel
    ON program_notification_settings(master_program_id, channel)
    WHERE effective_to IS NULL;

CREATE INDEX IF NOT EXISTS idx_pns_enabled_lookup
    ON program_notification_settings(master_program_id, channel, is_enabled)
    WHERE effective_to IS NULL;

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

CREATE TRIGGER trg_master_programs_updated_at
    BEFORE UPDATE ON master_programs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_contacts_updated_at
    BEFORE UPDATE ON contacts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_pns_updated_at ON program_notification_settings;
CREATE TRIGGER trg_pns_updated_at
    BEFORE UPDATE ON program_notification_settings
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- Audit: deleted rows
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_deleted_records (
    id             BIGSERIAL    PRIMARY KEY,
    table_name     TEXT         NOT NULL,
    deleted_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_by     TEXT         NOT NULL DEFAULT current_user,
    deleted_row_id TEXT,
    row_data       JSONB        NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_deleted_records_table_name ON audit_deleted_records(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_deleted_records_deleted_at ON audit_deleted_records(deleted_at);

CREATE OR REPLACE FUNCTION audit_delete()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_deleted_records (table_name, deleted_row_id, row_data)
    VALUES (
        TG_TABLE_NAME,
        OLD.id::TEXT,
        to_jsonb(OLD)
    );

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_master_programs_audit_delete ON master_programs;
CREATE TRIGGER trg_master_programs_audit_delete
    BEFORE DELETE ON master_programs
    FOR EACH ROW EXECUTE FUNCTION audit_delete();

DROP TRIGGER IF EXISTS trg_import_jobs_audit_delete ON import_jobs;
CREATE TRIGGER trg_import_jobs_audit_delete
    BEFORE DELETE ON import_jobs
    FOR EACH ROW EXECUTE FUNCTION audit_delete();

DROP TRIGGER IF EXISTS trg_contacts_audit_delete ON contacts;
CREATE TRIGGER trg_contacts_audit_delete
    BEFORE DELETE ON contacts
    FOR EACH ROW EXECUTE FUNCTION audit_delete();

DROP TRIGGER IF EXISTS trg_message_logs_audit_delete ON message_logs;
CREATE TRIGGER trg_message_logs_audit_delete
    BEFORE DELETE ON message_logs
    FOR EACH ROW EXECUTE FUNCTION audit_delete();

DROP TRIGGER IF EXISTS trg_pns_audit_delete ON program_notification_settings;
CREATE TRIGGER trg_pns_audit_delete
    BEFORE DELETE ON program_notification_settings
    FOR EACH ROW EXECUTE FUNCTION audit_delete();
