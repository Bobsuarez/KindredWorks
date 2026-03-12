package com.kinforgework.cplaneta.service.notification.strategy.concrete;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.integration.whatsapp.WhatsAppService;
import com.kinforgework.cplaneta.service.notification.strategy.NotificationChannelStrategy;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WhatsappChannel implements NotificationChannelStrategy {

    private static final String WHATSAPP_BODY = "revisa tu correo";

    private final WhatsAppService whatsAppService;

    @Override
    public ChannelType getType() {
        return ChannelType.WHATSAPP;
    }

    @Override
    public DeliveryResult send(ContactEntity contactEntity, MasterProgramEntity program) {
        try {
            whatsAppService.sendTextMessage(
                    contactEntity.getPhoneNumber(),
                    WHATSAPP_BODY
            );
            return DeliveryResult.success(ChannelType.WHATSAPP);
        } catch (Exception ex) {
            log.error("WhatsApp failed for contact id={}: {}", contactEntity.getId(), ex.getMessage());
            return DeliveryResult.failure(ChannelType.WHATSAPP, ex);
        }
    }
}
