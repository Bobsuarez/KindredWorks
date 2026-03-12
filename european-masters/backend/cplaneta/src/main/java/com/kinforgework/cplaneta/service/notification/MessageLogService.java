package com.kinforgework.cplaneta.service.notification;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.entities.MessageLogEntity;
import com.kinforgework.cplaneta.repository.MessageLogRepository;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    public void record(
            ContactEntity contactEntity,
            MasterProgramEntity program,
            List<DeliveryResult> results
    ) {

        DeliveryResult email = find(ChannelType.EMAIL, results);
        DeliveryResult whatsapp = find(ChannelType.WHATSAPP, results);

        MessageLogEntity log = MessageLogEntity.builder()
                .contact(contactEntity)
                .masterProgram(program)
                .emailStatus(email.getStatus())
                .whatsappStatus(whatsapp.getStatus())
                .emailSentAt(email.getSentAt())
                .whatsappSentAt(whatsapp.getSentAt())
                .errorMessage(buildErrors(results))
                .build();

        messageLogRepository.save(log);
    }

    private DeliveryResult find(ChannelType type, List<DeliveryResult> results) {
        return results.stream()
                .filter(r -> r.getChannel() == type)
                .findFirst()
                .orElse(null);
    }

    private String buildErrors(List<DeliveryResult> results) {
        return results.stream()
                .map(DeliveryResult::getError)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
    }
}
