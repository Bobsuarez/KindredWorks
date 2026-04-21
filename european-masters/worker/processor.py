import os
import pandas as pd
from db import (
    get_connection,
    get_or_create_programa,
    insertar_contactos,
    actualizar_progreso,
    actualizar_total_rows
)
from normalizer import normalizar_telefono_y_pais, normalizar_correo, normalizar_nombre

CHUNK_SIZE = 10_000

# Mapeo flexible — ajusta a los nombres reales de tus columnas
COLUMNAS_MAP = {
    "Nombre completo (Contacto) (Contacto)": "nombre",
    "Teléfono (Contacto) (Contacto)":        "telefono",
    "Correo":                                 "correo",
    "Nombre del programa (Programa de Interes) (Programa)": "programa"
}

def detectar_columnas(df_columns: list) -> dict:
    """
    Busca las columnas requeridas de forma flexible
    (por si el nombre cambia levemente entre archivos).
    """
    mapa = {}
    requeridas = {
        "nombre":   ["nombre completo", "nombre", "Nombre completo"],
        "telefono": ["teléfono", "telefono", "phone", "Teléfono"],
        "correo":   ["correo", "email", "e-mail", "Email"],
        "programa": ["nombre del programa", "programa", "Nombre del programa"]
    }
    columnas_lower = {c.lower(): c for c in df_columns}

    for campo, candidatos in requeridas.items():
        for candidato in candidatos:
            match = next((orig for low, orig in columnas_lower.items() if candidato in low), None)
            if match:
                mapa[match] = campo
                break

    return mapa

def procesar_archivo(job_id: str, filename: str):
    upload_dir = os.environ["UPLOAD_DIR"]
    print(f"Directorio de upload: {upload_dir}")
    print(f"Procesando archivo: {filename}")
    filepath = os.path.join(upload_dir, filename)
    print(f"Ruta del archivo: {filepath}")
    extension = filename.split(".")[-1].lower()
    print(f"Extensión del archivo: {extension}")

    conn = get_connection()
    cur = conn.cursor()

    processed = 0
    errors = 0

    try:
        # --- Seleccionar reader según extensión ---
        if extension == "csv":
            # CSV sí devuelve un iterador con chunksize
            reader = pd.read_csv(
                filepath,
                dtype=str,
                chunksize=CHUNK_SIZE,
                encoding="utf-8",
                encoding_errors="replace"
            )
        else:
            # Excel NO soporta chunksize. Lo leemos todo y lo dividimos en pedazos.
            full_df = pd.read_excel(
                filepath,
                dtype=str,
                engine="openpyxl"
            )
            # Creamos una lista de chunks manualmente
            reader = [full_df[i:i + CHUNK_SIZE] for i in range(0, full_df.shape[0], CHUNK_SIZE)]

        columnas_detectadas = None

        for chunk in reader:
            # Detectar mapeo de columnas en el primer chunk
            if columnas_detectadas is None:
                columnas_detectadas = detectar_columnas(list(chunk.columns))
                if not columnas_detectadas:
                    raise ValueError(f"No se encontraron columnas requeridas en: {list(chunk.columns)}")

            chunk.rename(columns=columnas_detectadas, inplace=True)

            # Ignorar filas sin nombre ni correo
            chunk.dropna(subset=["nombre", "correo"], inplace=True)

            if chunk.empty:
                continue

            # Normalizar
            chunk["nombre"]   = chunk["nombre"].apply(normalizar_nombre)
            if "telefono" in chunk.columns:
                telefono_pais = chunk["telefono"].apply(normalizar_telefono_y_pais)
                chunk["telefono"] = telefono_pais.apply(lambda x: x[0])
                chunk["pais"] = telefono_pais.apply(lambda x: x[1])
            else:
                chunk["telefono"] = None
                chunk["pais"] = "Desconocido"
            chunk["correo"]   = chunk["correo"].apply(normalizar_correo)
            chunk["programa"] = chunk.get("programa", pd.Series(["Sin programa"] * len(chunk))).fillna("Sin programa")

            # Construir filas para INSERT
            contactos = []
            for _, row in chunk.iterrows():
                try:
                    pid = get_or_create_programa(cur, row["programa"].strip())
                    contactos.append((
                        row["nombre"],
                        row.get("telefono"),
                        row.get("pais", "Desconocido"),
                        row["correo"],
                        pid
                    ))
                except Exception as e:
                    print(f"[{job_id}] ERROR al obtener programa: {e}")
                    errors += 1

            # Insertar lote
            try:
                insertar_contactos(cur, contactos)
                processed += len(contactos)
            except Exception as e:
                print(f"[{job_id}] ERROR al insertar contactos: {e}")
                errors += len(contactos)
                conn.rollback()
                continue

            # Actualizar progreso cada chunk
            actualizar_progreso(cur, job_id, processed, errors)
            conn.commit()

            print(f"[{job_id}] Procesados: {processed:,} | Errores: {errors:,}")

        # Finalizar
        actualizar_progreso(cur, job_id, processed, errors, status="COMPLETED")
        conn.commit()
        print(f"[{job_id}] COMPLETADO — {processed:,} registros")

    except Exception as e:
        print(f"[{job_id}] ERROR FATAL: {e}")
        actualizar_progreso(cur, job_id, processed, errors, status="FAILED")
        conn.commit()
        raise

    finally:
        # Limpiar archivo del volumen al terminar
        try:
            os.remove(filepath)
        except OSError:
            pass
        cur.close()
        conn.close()
        conn.close()