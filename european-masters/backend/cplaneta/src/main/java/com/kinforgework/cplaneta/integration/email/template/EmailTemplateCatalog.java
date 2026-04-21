package com.kinforgework.cplaneta.integration.email.template;

import com.kinforgework.cplaneta.utils.TemplateResolverUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailTemplateCatalog {

    private final TemplateResolverUtil templateResolverUtil;

    @Value("classpath:email-templates.json")
    private Resource templatesResource;

    @Value("${app.email-templates.redis-key-prefix:email:templates}")
    private String redisKeyPrefix;

    private List<String> introTemplates = List.of("Gracias por su interes en nuestros programas de posgrado. Queremos compartir con usted informacion detallada sobre el programa:");
    private List<String> valueTemplates = List.of("Este programa esta dirigido a profesionales que buscan fortalecer competencias, actualizar su perfil y acceder a mejores oportunidades laborales.");
    private List<String> curriculumTemplates = List.of("En la malla curricular adjunta encontrara modulos, asignaturas principales, duracion estimada y resultados de aprendizaje.");
    private List<String> ctaTemplates = List.of("Si desea una asesoria personalizada sobre admision, descuentos o facilidades de pago, puede responder este correo o escribirnos por WhatsApp.");
    private List<String> closingTemplates = List.of("Sera un gusto acompanarle en su proceso de formacion de posgrado.");

    @PostConstruct
    void loadTemplates() {
        introTemplates = templateResolverUtil.loadFromRedisOrFile(redisKeyPrefix + ":intro", templatesResource, "intro", introTemplates);
        valueTemplates = templateResolverUtil.loadFromRedisOrFile(redisKeyPrefix + ":value", templatesResource, "value", valueTemplates);
        curriculumTemplates = templateResolverUtil.loadFromRedisOrFile(redisKeyPrefix + ":curriculum", templatesResource, "curriculum", curriculumTemplates);
        ctaTemplates = templateResolverUtil.loadFromRedisOrFile(redisKeyPrefix + ":cta", templatesResource, "cta", ctaTemplates);
        closingTemplates = templateResolverUtil.loadFromRedisOrFile(redisKeyPrefix + ":closing", templatesResource, "closing", closingTemplates);
    }

    public int randomIndex() {
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(count());
    }

    public int count() {
        return minSize(introTemplates, valueTemplates, curriculumTemplates, ctaTemplates, closingTemplates);
    }

    public String buildTextBody(String recipientName, String programName, int templateIndex) {
        return """
                Hola %s,
                
                %s
                %s.
                
                %s
                %s
                
                Atentamente,
                Oficina de Posgrados
                """.formatted(
                recipientName,
                introTemplates.get(templateIndex),
                programName,
                curriculumTemplates.get(templateIndex),
                ctaTemplates.get(templateIndex)
        );
    }

    public String buildDecoratedBody(String recipientName, String programName, int templateIndex) {
        EmailBodyComponent component = new ClosingDecorator(
                new CtaDecorator(
                        new CurriculumDecorator(
                                new ValueDecorator(
                                        new IntroDecorator(
                                                new GreetingComponent(),
                                                introTemplates.get(templateIndex)
                                        ),
                                        valueTemplates.get(templateIndex)
                                ),
                                curriculumTemplates.get(templateIndex)
                        ),
                        ctaTemplates.get(templateIndex)
                ),
                closingTemplates.get(templateIndex)
        );
        return component.render(recipientName, programName);
    }

    private int minSize(List<String>... groups) {
        int min = Integer.MAX_VALUE;
        for (List<String> group : groups) {
            min = Math.min(min, group.size());
        }
        return Math.max(min, 1);
    }
}
