package com.kinforgework.cplaneta.service.notification.strategy.context;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.entities.MessageLogEntity;
import com.kinforgework.cplaneta.enums.ContactStatus;
import com.kinforgework.cplaneta.enums.DeliveryStatus;
import com.kinforgework.cplaneta.repository.ContactRepository;
import com.kinforgework.cplaneta.service.notification.ContactQueueService;
import com.kinforgework.cplaneta.service.notification.MessageLogService;
import com.kinforgework.cplaneta.service.notification.strategy.NotificationChannelStrategy;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import com.kinforgework.cplaneta.service.ProgramNotificationSettingService;
import com.kinforgework.cplaneta.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates a single dispatch cycle:
 * <ol>
 *   <li>Fetches the next PENDING contact</li>
 *   <li>Sends email and WhatsApp</li>
 *   <li>Persists a {@link MessageLogEntity} with per-channel outcomes</li>
 *   <li>Updates the contact status</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageDispatchService {

    private final ContactRepository contactRepository;
    private final ContactQueueService contactQueueService;
    private final MessageLogService logService;
    private final List<NotificationChannelStrategy> channels;
    private final ProgramNotificationSettingService notificationSettingService;

    /**
     * Processes exactly one pending contact.
     *
     * @return {@code true} if a contact was found and processed; {@code false} if the queue is empty.
     */
    @Transactional
    public boolean dispatchNext() {
        Optional<ContactEntity> maybeContact = contactQueueService.nextPending();

        if (maybeContact.isEmpty()) {
            log.info("No pending contacts in queue. Skipping cycle.");
            return Boolean.FALSE;
        }

        ContactEntity contactEntity = maybeContact.get();
        MasterProgramEntity program = contactEntity.getMasterProgram();

        log.info(
                "Dispatching contact masterProgramId={} email='{}' program='{}'", contactEntity.getId(), contactEntity.getEmail(),
                program.getName()
        );

        List<DeliveryResult> results = new ArrayList<>();

        for (NotificationChannelStrategy channel : channels) {
            NotificationChannel notificationChannel = mapChannel(channel.getType());
            boolean enabled = notificationSettingService.isChannelEnabled(program.getId(), notificationChannel);
            if (!enabled) {
                results.add(DeliveryResult.disabled(
                        channel.getType(),
                        "Channel disabled for program " + program.getId()
                ));
                continue;
            }
            results.add(channel.send(contactEntity, program));
        }

        logService.record(contactEntity, program, results);

        boolean success = results.stream()
                .allMatch(r -> r.getStatus() != DeliveryStatus.FAILED);

        contactEntity.setStatus(success ? ContactStatus.SENT : ContactStatus.ERROR);

        log.info("Dispatch complete for contact masterProgramId={}. status={}", contactEntity.getId(), success);

        contactRepository.save(contactEntity);

        return Boolean.TRUE;
    }

    private NotificationChannel mapChannel(ChannelType channelType) {
        return NotificationChannel.valueOf(channelType.name());
    }
}
