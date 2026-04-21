package com.kinforgework.cplaneta.service.notification.strategy.concrete;

import com.kinforgework.cplaneta.entities.ContactEntity;
import com.kinforgework.cplaneta.entities.MasterProgramEntity;
import com.kinforgework.cplaneta.integration.whatsapp.WhatsAppService;
import com.kinforgework.cplaneta.service.notification.strategy.NotificationChannelStrategy;
import com.kinforgework.cplaneta.service.notification.strategy.dtos.DeliveryResult;
import com.kinforgework.cplaneta.service.notification.strategy.enums.ChannelType;
import com.kinforgework.cplaneta.utils.TemplateResolverUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class WhatsappChannel implements NotificationChannelStrategy {

    private static final String DEFAULT_WHATSAPP_BODY = "Hola {name}, revisa tu correo sobre el programa {program}.";

    private final WhatsAppService whatsAppService;
    private final TemplateResolverUtil templateResolverUtil;

    @Value("classpath:whatsapp-templates.json")
    private Resource templatesResource;

    @Value("${app.whatsapp-templates.redis-key:whatsapp:templates}")
    private String redisTemplatesKey;

    private List<String> templates = List.of(DEFAULT_WHATSAPP_BODY);

    @PostConstruct
    void loadTemplates() {
        templates = templateResolverUtil.loadFromRedisOrFile(
                redisTemplatesKey,
                templatesResource,
                "templates",
                List.of(DEFAULT_WHATSAPP_BODY)
        );
        log.info("Templates de WhatsApp disponibles: {}", templates.size());
    }

    @Override
    public ChannelType getType() {
        return ChannelType.WHATSAPP;
    }

    @Override
    public DeliveryResult send(ContactEntity contactEntity, MasterProgramEntity program) {
        try {

            String body = buildWhatsappBody(contactEntity, program);

            whatsAppService.sendTextMessage(
                    contactEntity.getPhoneNumber(),
                    body
            );

            return DeliveryResult.success(ChannelType.WHATSAPP);
        } catch (Exception ex) {
            log.error("WhatsApp failed for contact id={}: {}", contactEntity.getId(), ex.getMessage());
            return DeliveryResult.failure(ChannelType.WHATSAPP, ex);
        }
    }

    private String buildWhatsappBody(ContactEntity contactEntity, MasterProgramEntity program) {
        String template = templateResolverUtil.randomOne(templates);
        String name = contactEntity.getName() == null ? "" : contactEntity.getName().trim();
        String programName = program.getName() == null ? "" : program.getName().trim();
        return template
                .replace("{name}", name)
                .replace("{program}", programName);
    }
}
