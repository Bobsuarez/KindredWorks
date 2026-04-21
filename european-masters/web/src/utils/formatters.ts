/**
 * Formatea una cadena de fecha a un formato regional de España con hora y minutos.
 * @param dateStr Cadena de fecha (ISO o compatible con Date)
 * @returns Fecha formateada (ej. 21/04/2026, 14:30)
 */
export const formatDate = (dateStr: string | null | undefined): string => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  });
};

/**
 * Formatea un ID numérico con un prefijo # y ceros a la izquierda.
 * @param id Identificador numérico
 * @param padding Número de ceros (por defecto 3)
 * @returns ID formateado (ej. #001)
 */
export const formatId = (id: number | null | undefined, padding: number = 3): string => {
  if (id === null || id === undefined) return '';
  return `#${String(id).padStart(padding, '0')}`;
};
