package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.service.notification.strategy.context.MessageDispatchService;
import com.kinforgework.cplaneta.utils.HoursValidatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;


@RequiredArgsConstructor
@Service
@Slf4j
public class MessageSchedulerService {

    private static final Duration INTERVAL_MINUTES = Duration.ofMinutes(3);
    private final TaskScheduler taskScheduler;
    private final MessageDispatchService messageDispatchService;

    private volatile ScheduledFuture<?> currentTask;
    private volatile boolean running = false;

    /**
     * Llamado desde el API endpoint para iniciar el proceso
     */
    public synchronized void start() {
        if (running) {
            log.warn("Scheduler ya está en ejecución.");
            return;
        }
        running = true;
        log.info("Scheduler iniciado.");
        scheduleNextExecution();
    }

    private void scheduleNextExecution() {
        if (!running) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();

        ZonedDateTime slot = HoursValidatorUtil.isWithinBusinessHours(now)
                ? now.plus(INTERVAL_MINUTES)
                : HoursValidatorUtil.nextValidSlot(now);

        Duration delay = Duration.between(now, slot);
        log.info("Próxima ejecución programada en: {} minutos", delay.toMinutes());

        currentTask = taskScheduler.schedule(
                this::executeNext,
                Instant.now()
                        .plusMillis(delay.toMillis())
        );
    }

    private void executeNext() {
        if (!running) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();
        if (!HoursValidatorUtil.isWithinBusinessHours(now)) {
            log.info("Fuera de horario laboral. Programando próximo slot...");
            scheduleNextExecution();
            return;
        }

        // Obtener siguiente contacto con al menos un canal pendiente
        boolean isDispatchNext = messageDispatchService.dispatchNext();

        if (!isDispatchNext) {
            shutdown();
            return;
        }
        scheduleNextExecution();
    }

    private void shutdown() {
        running = false;
        if (currentTask != null) {
            currentTask.cancel(false);
        }
        log.info("Scheduler detenido. Razón: {}", "No quedan registros pendientes.");
    }
}
