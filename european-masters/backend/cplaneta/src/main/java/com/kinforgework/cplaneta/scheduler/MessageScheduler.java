package com.kinforgework.cplaneta.scheduler;

import com.kinforgework.cplaneta.repository.MessageLogRepository;
import com.kinforgework.cplaneta.service.notification.strategy.context.MessageDispatchService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.ScheduledFuture;

/**
 * Drives automated dispatch of promotional messages.
 *
 * <h3>Schedule constraints</h3>
 * <ul>
 *   <li>Fixed delay: 144 000 ms (2.4 minutes) between the end of one execution and the start of the next</li>
 *   <li>Active window: Monday–Friday, 07:00–19:00 (local time)</li>
 *   <li>Daily cap: {@code app.scheduling.daily-limit} messages (default 300)</li>
 * </ul>
 *
 * <h3>Why fixedDelay and not cron?</h3>
 * Spring's built-in cron expression only supports 1-second granularity, making
 * 2.4-minute intervals impossible to express exactly. {@code fixedDelay} operates
 * in milliseconds and guarantees the interval between consecutive executions,
 * regardless of how long the previous execution took.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageScheduler {

    private final MessageDispatchService messageDispatchService;
    private final MessageLogRepository messageLogRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    @Value("${app.scheduling.fixed-delay-ms:144000}")
    private long fixedDelayMs;

    @Value("${app.scheduling.window-start-hour:7}")
    private int windowStartHour;

    @Value("${app.scheduling.window-end-hour:19}")
    private int windowEndHour;

    @Value("${app.scheduling.daily-limit:300}")
    private int dailyLimit;

    // Referencia a la tarea para poder cancelarla
    private ScheduledFuture<?> scheduledTask;

    @PostConstruct
    public void start() {
        log.info("Iniciando MessageScheduler con intervalo de {}ms", fixedDelayMs);
        scheduleTask();
    }

    private void scheduleTask() {
        scheduledTask = taskScheduler.scheduleWithFixedDelay(this::run, fixedDelayMs);
    }

    /**
     * Executes every 2.4 minutes (144 000 ms fixed delay).
     * The delay is measured from the moment the previous execution finishes,
     * so even if dispatch takes several seconds the cadence stays safe.
     */
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Running MessageScheduler" + now);

        if (!isWithinActiveWindow(now)) {
            log.info("Outside active window ({}). Skipping.", now);
            return;
        }

        if (isDailyLimitReached(now.toLocalDate())) {
            log.info("Daily dispatch limit of {} reached. Skipping remainder of window.", dailyLimit);
            return;
        }

        log.debug("Scheduler tick at {}", now);
        boolean hadContact = messageDispatchService.dispatchNext();

        if (!hadContact) {
            log.info("No hay más contactos PENDING. Deteniendo el scheduler.");
            stop();
        }
    }

    public void stop() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false); // false = espera que el ciclo actual termine
            log.info("Scheduler detenido correctamente.");
        }
    }

    /**
     * Permite reactivar el scheduler externamente si se cargan nuevos contactos.
     */
    public void restart() {
        stop();
        scheduleTask();
        log.info("Scheduler reiniciado.");
    }

    // -------------------------------------------------------------------------
    // Guard conditions
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code dateTime} falls within Monday–Friday
     * and between the configured start and end hours (inclusive start, exclusive end).
     */
    private boolean isWithinActiveWindow(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;

        int hour = dateTime.getHour();
        boolean isInHourWindow = hour >= windowStartHour && hour < windowEndHour;

        return isWeekday && isInHourWindow;
    }

    /**
     * Counts MessageLog entries created today and compares against {@code dailyLimit}.
     */
    private boolean isDailyLimitReached(LocalDate today) {
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime startOfDay = today.atStartOfDay(zone)
                .toOffsetDateTime();
        OffsetDateTime endOfDay = today.plusDays(1)
                .atStartOfDay(zone)
                .toOffsetDateTime();

        long dispatched = messageLogRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        return dispatched >= dailyLimit;
    }
}
