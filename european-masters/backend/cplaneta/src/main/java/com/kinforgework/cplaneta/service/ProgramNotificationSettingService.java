package com.kinforgework.cplaneta.service;

import com.kinforgework.cplaneta.dto.NotificationChannelStatusResponse;
import com.kinforgework.cplaneta.dto.ProgramChannelUpdate;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.entities.ProgramNotificationSettingEntity;
import com.kinforgework.cplaneta.enums.NotificationChannel;
import com.kinforgework.cplaneta.repository.MasterProgramRepository;
import com.kinforgework.cplaneta.repository.ProgramNotificationSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramNotificationSettingService {

    private final ProgramNotificationSettingRepository settingRepository;
    private final MasterProgramRepository masterProgramRepository;

    /**
     * Actualiza o inserta el estado de los canales para varios programas.
     */
    @Transactional
    public List<NotificationChannelStatusResponse> upsertChannelStatus(List<ProgramChannelUpdate> requests) {
        List<ProgramNotificationSettingEntity> toSave = new ArrayList<>();

        for (ProgramChannelUpdate request : requests) {
            MasterProgramEntity program = masterProgramRepository.findById(request.masterProgramId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "MasterProgram no encontrado: " + request.masterProgramId()));

            request.channels()
                    .forEach((channelName, enabled) -> {
                        try {
                            NotificationChannel channel = NotificationChannel.valueOf(channelName.toUpperCase());

                            // Buscar si ya existe la configuración para este programa y canal
                            ProgramNotificationSettingEntity setting = settingRepository
                                    .findAllActiveByMasterProgramIn(List.of(program.getId()))
                                    .stream()
                                    .filter(s -> s.getChannel() == channel)
                                    .findFirst()
                                    .orElseGet(() -> crearNuevaConfiguracion(program, channel));

                            setting.setIsEnabled(enabled);
                            setting.setUpdatedAt(OffsetDateTime.now());
                            toSave.add(setting);

                        } catch (IllegalArgumentException e) {
                            log.warn("Canal de notificación no válido: {}", channelName);
                        }
                    });
        }

        List<ProgramNotificationSettingEntity> saved = settingRepository.saveAll(toSave);
        return saved.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProgramNotificationSettingEntity crearNuevaConfiguracion(
            MasterProgramEntity program, NotificationChannel channel) {
        return ProgramNotificationSettingEntity.builder()
                .masterProgram(program)
                .channel(channel)
                .isEnabled(true)
                .effectiveFrom(OffsetDateTime.now())
                .updatedBy("SYSTEM")
                .reason("Auto-generated during upsert")
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isChannelEnabled(Long masterProgramId, NotificationChannel channel) {
        // Asumimos que si no hay configuración, el canal está habilitado por defecto
        return settingRepository.findAllActiveByMasterProgramIn(List.of(masterProgramId))
                .stream()
                .filter(s -> s.getChannel() == channel)
                .findFirst()
                .map(ProgramNotificationSettingEntity::getIsEnabled)
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public List<NotificationChannelStatusResponse> findAllActiveStatuses() {
        return settingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationChannelStatusResponse toResponse(ProgramNotificationSettingEntity entity) {
        return new NotificationChannelStatusResponse(
                entity.getMasterProgram().getId(),
                entity.getMasterProgram().getName(),
                0L,
                entity.getChannel(),
                entity.getIsEnabled(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getUpdatedAt(),
                entity.getReason()
        );
    }
}
