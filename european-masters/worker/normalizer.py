import re
import pandas as pd
import phonenumbers
from phonenumbers.phonenumberutil import NumberParseException

REGIONES_INTENTO = ("CO", "PE", "MX", "AR", "CL", "EC", "US", "ES")

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

    # Si ya trae prefijo internacional, conservar/normalizar a E.164.
    if tiene_plus:
        try:
            numero = phonenumbers.parse('+' + digitos, None)
            if phonenumbers.is_possible_number(numero):
                telefono_e164 = phonenumbers.format_number(
                    numero, phonenumbers.PhoneNumberFormat.E164
                )
                return telefono_e164.lstrip('+')
        except NumberParseException:
            pass
        return digitos

    # Si no trae prefijo, intentar detectar el país por región.
    for region in REGIONES_INTENTO:
        try:
            numero = phonenumbers.parse(digitos, region)
            if phonenumbers.is_valid_number(numero):
                telefono_e164 = phonenumbers.format_number(
                    numero, phonenumbers.PhoneNumberFormat.E164
                )
                return telefono_e164.lstrip('+')
        except NumberParseException:
            continue

    # Fallback: si no se pudo detectar, usar +57 para locales sin prefijo.
    return '57' + digitos

def normalizar_telefono_y_pais(valor):
    if pd.isna(valor):
        return None, "Desconocido"

    raw = str(valor).strip()
    if not raw:
        return None, "Desconocido"

    try:
        raw = str(int(float(raw)))
    except ValueError:
        pass

    tiene_plus = raw.startswith('+')
    digitos = re.sub(r'\D', '', raw)
    if not digitos:
        return raw, "Desconocido"

    if tiene_plus:
        try:
            numero = phonenumbers.parse('+' + digitos, None)
            if phonenumbers.is_valid_number(numero):
                telefono = phonenumbers.format_number(
                    numero, phonenumbers.PhoneNumberFormat.E164
                )
                pais = phonenumbers.region_code_for_number(numero) or "Desconocido"
                return telefono.lstrip('+'), pais
        except NumberParseException:
            pass
        return raw, "Desconocido"

    for region in REGIONES_INTENTO:
        try:
            numero = phonenumbers.parse(digitos, region)
            if phonenumbers.is_valid_number(numero):
                telefono = phonenumbers.format_number(
                    numero, phonenumbers.PhoneNumberFormat.E164
                )
                pais = phonenumbers.region_code_for_number(numero) or "Desconocido"
                return telefono.lstrip('+'), pais
        except NumberParseException:
            continue

    return raw, "Desconocido"

def normalizar_correo(valor):
    if pd.isna(valor):
        return None
    return str(valor).strip().lower()

def normalizar_nombre(valor):
    if pd.isna(valor):
        return None
    return str(valor).strip().title()