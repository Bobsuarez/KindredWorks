package com.kinforgework.cplaneta.integration.email;

import com.kinforgework.cplaneta.integration.email.template.EmailTemplateCatalog;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class JakartaMailEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateCatalog emailTemplateCatalog;

    @Value("${app.email.from-address}")
    private String fromAddress;

    @Value("${app.email.from-name}")
    private String fromName;

    @Override
    public void sendPromotionalEmail(
            String to,
            String recipientName,
            String programName,
            File subjectImage,
            File curriculumPdf
    ) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart = true enables attachments; true = multipart
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Cabeceras suaves: evitamos marcarnos como spam masivo agresivo
            helper.setValidateAddresses(true);
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject("Programa de Maestría: " + programName);

            // Versión texto plano + HTML para mejorar entregabilidad
            int templateIndex = emailTemplateCatalog.randomIndex();
            String textBody = buildTextBody(recipientName, programName, templateIndex);

            boolean includeSubjectImage = subjectImage != null && subjectImage.exists() && subjectImage.isFile();

            String htmlBody = buildHtmlBody(recipientName, programName, templateIndex, includeSubjectImage);
            helper.setText(textBody, htmlBody);

            // 2) Portada como imagen inline
            helper.addInline("portadaImage", new ClassPathResource("img/portada.png"));

            // Inline subject image (referenced in HTML via Content-ID), optional
            if (includeSubjectImage) {
                FileSystemResource imageResource = new FileSystemResource(subjectImage);
                helper.addInline("subjectImage", imageResource);
            }

            // 4) Botón de WhatsApp (también desde resources: img/whatsapp.png)
            helper.addInline("whatsappButton", new ClassPathResource("img/whatsapp.png"));

            // PDF curriculum as attachment
            FileSystemResource pdfResource = new FileSystemResource(curriculumPdf);
            helper.addAttachment("Malla_" + sanitizeFileName(programName) + ".pdf", pdfResource);

            mailSender.send(message);

            log.info("Email sent successfully to '{}' for program '{}'", to, programName);

        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("Failed to send email to '{}': {}", to, ex.getMessage(), ex);
            throw new RuntimeException("Email delivery failed for recipient: " + to, ex);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------
    private String buildTextBody(String recipientName, String programName, int templateIndex) {
        return emailTemplateCatalog.buildTextBody(recipientName, programName, templateIndex);
    }

    private String buildHtmlBody(
            String recipientName, String programName, int templateIndex, boolean includeSubjectImage) {
        String dynamicBody = emailTemplateCatalog.buildDecoratedBody(recipientName, programName, templateIndex);
        String subjectImageBlock = includeSubjectImage
                ? """
                          <!-- Imagen del programa (subjectImage) -->
                          <tr>
                            <td align="center" style="padding:0 24px 24px 24px;">
                              <img src="cid:subjectImage" alt="Imagen del programa" style="display:block; max-width:600px; width:100%%; border:0;"/>
                            </td>
                          </tr>
                """
                : "";

        return """
                <html>
                <body style="margin:0; padding:0; background-color:#f5f5f5;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td align="center" style="padding:20px 0;">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="background-color:#ffffff; font-family: Arial, sans-serif; color:#333;">
                          <!-- Portada -->
                          <tr>
                            <td align="center">
                              <img src="cid:portadaImage" alt="Portada programa" style="display:block; width:100%%; max-width:600px; border:0;"/>
                            </td>
                          </tr>
                
                          <!-- Cuerpo de texto principal -->
                          <tr>
                            <td style="padding:24px;">
                              <ul style="margin:0 0 16px 20px; padding:0; font-size:14px; line-height:1.6;">
                                <li>Docentes con amplia experiencia académica y profesional.</li>
                                <li>Enfoque práctico orientado a la solución de problemas reales.</li>
                                <li>Red de contactos con otros profesionales y organizaciones.</li>
                                <li>Acceso a recursos académicos y acompañamiento permanente.</li>
                              </ul>
                              %s
                            </td>
                          </tr>
                
                          %s
                
                          <!-- Botón de WhatsApp como imagen clicable -->
                          <tr>
                            <td align="center" style="padding:0 0 32px 0;">
                              <a href="https://wa.me/+34936069310" style="text-decoration:none;" target="_blank">
                                <img src="cid:whatsappButton"
                                     alt="Contactar por WhatsApp"
                                     style="display:block; border:0; max-width:250px; width:60%%;"/>
                              </a>
                            </td>
                          </tr>
                
                          <!-- Firma -->
                          <tr>
                            <td align="center" style="padding:0 24px 24px 24px;">
                              <p style="margin:0; font-size:14px; line-height:1.6;">
                                Atentamente,<br/>
                                <strong>Oficina de Posgrados</strong>
                              </p>
                            </td>
                          </tr>
                
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(dynamicBody, subjectImageBlock);
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
