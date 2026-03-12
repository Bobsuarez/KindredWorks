package com.kinforgework.cplaneta.service.notification.strategy.dtos;

import com.kinforgework.cplaneta.enums.DeliveryStatus;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
public class DeliveryResult {

    private final ChannelType channel;
    private final DeliveryStatus status;
    private final OffsetDateTime sentAt;
    private final String error;

    public static DeliveryResult success(ChannelType channel) {
        return new DeliveryResult(channel, DeliveryStatus.SUCCESS, OffsetDateTime.now(), null);
    }

    public static DeliveryResult failure(ChannelType channel, Exception ex) {
        return new DeliveryResult(channel, DeliveryStatus.FAILED, null, ex.getMessage());
    }
}
