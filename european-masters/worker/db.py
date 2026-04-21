import os
import psycopg2
from psycopg2.extras import execute_batch

def get_connection():
    return psycopg2.connect(
        host=os.environ["DB_HOST"],
        port=os.environ.get("DB_PORT", 5432),
        dbname=os.environ["DB_NAME"],
        user=os.environ["DB_USER"],
        password=os.environ["DB_PASSWORD"]
    )

# Cache en memoria para evitar SELECTs repetidos
_programa_cache = {}

def get_or_create_programa(cur, nombre):
    if nombre in _programa_cache:
        return _programa_cache[nombre]

    cur.execute("""
        INSERT INTO master_programs (name)
        VALUES (%s)
        ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id
    """, (nombre,))

    pid = cur.fetchone()[0]
    _programa_cache[nombre] = pid
    return pid

def insertar_contactos(cur, contactos: list):
    execute_batch(cur, """
        INSERT INTO contacts (name, phone_number, country, email, master_program_id)
        VALUES (%s, %s, %s, %s, %s)
        ON CONFLICT (email) DO NOTHING
    """, contactos, page_size=1000)

def actualizar_progreso(cur, job_id: str, processed: int, errors: int, status: str = "PROCESSING"):
    cur.execute("""
        UPDATE import_jobs
        SET processed = %s,
            errors    = %s,
            status    = %s,
            finished_at = CASE WHEN %s IN ('COMPLETED','FAILED') THEN NOW() ELSE NULL END
        WHERE id = %s::uuid
    """, (processed, errors, status, status, job_id))

def actualizar_total_rows(cur, job_id: str, total: int):
    cur.execute("""
        UPDATE import_jobs SET total_rows = %s WHERE id = %s::uuid
    """, (total, job_id))