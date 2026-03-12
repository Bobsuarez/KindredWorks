package com.kinforgework.cplaneta.service.notification.strategy;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;

public interface NotificationChannelStrategy {

    ChannelType getType();

    DeliveryResult send(ContactEntity contactEntity, MasterProgramEntity program);

}
