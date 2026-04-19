import re
import pandas as pd

def normalizar_telefono(valor):
    if pd.isna(valor):
        return None
    raw = str(valor).strip()
    try:
        raw = str(int(float(raw)))
    except ValueError:
        pass
    tiene_plus = raw.startswith('+')
    digitos = re.sub(r'\D', '', raw)
    if not digitos:
        return None
    return ('+' + digitos) if tiene_plus else digitos

def normalizar_correo(valor):
    if pd.isna(valor):
        return None
    return str(valor).strip().lower()

def normalizar_nombre(valor):
    if pd.isna(valor):
        return None
    return str(valor).strip().title()