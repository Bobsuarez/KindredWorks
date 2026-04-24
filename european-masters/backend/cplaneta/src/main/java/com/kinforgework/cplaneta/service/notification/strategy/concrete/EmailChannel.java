package com.kinforgework.cplaneta.service.notification.strategy.concrete;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.integration.email.EmailService;
import com.kinforgework.cplaneta.service.FileStorageService;
import com.kinforgework.cplaneta.service.notification.strategy.NotificationChannelStrategy;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannelStrategy {

    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }

    @Override
    public DeliveryResult send(ContactEntity contactEntity, MasterProgramEntity program) {
        try {

            File image = resolveOptionalFile(program.getSubjectImagePath());
            File pdf = fileStorageService.resolveFile(program.getPdfCurriculumPath());

            emailService.sendPromotionalEmail(
                    contactEntity.getEmail(),
                    contactEntity.getName(),
                    program.getName(),
                    image,
                    pdf
            );

            return DeliveryResult.success(ChannelType.EMAIL);

        } catch (Exception ex) {
            log.error("Email failed for contact masterProgramId={}: {}", contactEntity.getId(), ex.getMessage());
            return DeliveryResult.failure(ChannelType.EMAIL, ex);
        }
    }

    private File resolveOptionalFile(String path) {
        if (path == null || path.isBlank() || "PENDING".equalsIgnoreCase(path)) {
            return null;
        }
        try {
            return fileStorageService.resolveFile(path);
        } catch (Exception ex) {
            log.warn("No se pudo cargar imagen opcional '{}': {}", path, ex.getMessage());
            return null;
        }
    }
}
