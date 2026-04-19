package com.kinforgework.cplaneta.utils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class HoursValidatorUtil {

    private static final LocalTime START = LocalTime.of(7, 0);
    private static final LocalTime END = LocalTime.of(19, 0);

    public static boolean isWithinBusinessHours(ZonedDateTime now) {
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        LocalTime time = now.toLocalTime();
        return !time.isBefore(START) && time.isBefore(END);
    }

    /**
     * Dado el momento actual, calcula cuándo es el próximo slot válido.
     * Usado para programar el delay cuando el scheduler "duerme".
     */
    public static ZonedDateTime nextValidSlot(ZonedDateTime now) {
        ZonedDateTime candidate = now;

        // Si ya pasó la hora de cierre hoy, ir al día siguiente
        if (candidate.toLocalTime()
                .isAfter(END) || candidate.toLocalTime()
                .equals(END)) {
            candidate = candidate.plusDays(1)
                    .with(START);
        }

        // Saltar fines de semana
        while (candidate.getDayOfWeek() == DayOfWeek.SATURDAY || candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidate = candidate.plusDays(1)
                    .with(START);
        }

        // Si estamos antes de las 7AM un día laboral
        if (candidate.toLocalTime()
                .isBefore(START)) {
            candidate = candidate.with(START);
        }

        return candidate;
    }
}
