import os
import json
import time
import redis
from processor import procesar_archivo

def conectar_redis(reintentos=10, espera=3):
    """Reintenta conectar a Redis — útil al arrancar con docker compose."""
    for intento in range(reintentos):
        try:
            r = redis.Redis(
                host=os.environ.get("REDIS_HOST", "localhost"),
                port=int(os.environ.get("REDIS_PORT", 6379)),
                decode_responses=True
            )
            r.ping()
            print("Conectado a Redis")
            return r
        except redis.ConnectionError:
            print(f"Redis no disponible, reintento {intento + 1}/{reintentos}...")
            time.sleep(espera)
    raise RuntimeError("No se pudo conectar a Redis")

def main():
    print("Worker ETL iniciado")
    r = conectar_redis()

    jobs_procesados = 0
    TIMEOUT_INACTIVIDAD = 30  # segundos sin trabajo → apagar

    while True:
        resultado = r.brpop("etl_jobs", timeout=TIMEOUT_INACTIVIDAD)

        if resultado is None:
            if jobs_procesados > 0:
                print(f"Sin jobs pendientes. Worker finalizado ({jobs_procesados} jobs procesados).")
                break  # sale del loop → el proceso termina → el contenedor se detiene
            continue

        _, mensaje = resultado
        job = json.loads(mensaje)
        print(f"Job recibido: {job['job_id']}/ {job["filename"]}")

        procesar_archivo(job["job_id"], job["filename"])
        jobs_procesados += 1


if __name__ == "__main__":
    main()