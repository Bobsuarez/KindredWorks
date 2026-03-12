package com.kinforgework.cplaneta.integration.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ClassPathResource;
import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class JakartaMailEmailService implements EmailService {

    private final JavaMailSender mailSender;

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
            String textBody = buildTextBody(recipientName, programName);
            String htmlBody = buildHtmlBody(recipientName, programName);
            helper.setText(textBody, htmlBody);

            // 2) Portada como imagen inline
            helper.addInline("portadaImage", new ClassPathResource("img/portada.png"));

            // Inline subject image (referenced in HTML via Content-ID)
            FileSystemResource imageResource = new FileSystemResource(subjectImage);
            helper.addInline("subjectImage", imageResource);

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
    private String buildTextBody(String recipientName, String programName) {
        return """
                Hola %s,

                Le invitamos a conocer más sobre nuestro programa de posgrado: %s.

                Adjuntamos la malla curricular completa del programa en formato PDF.
                También puede contactarnos directamente por WhatsApp para resolver cualquier duda
                o recibir asesoría sobre el proceso de admisión.

                Atentamente,
                Oficina de Posgrados
                """.formatted(recipientName, programName);
    }

    private String buildHtmlBody(String recipientName, String programName) {
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
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                Hola <strong>%s</strong>,
                              </p>
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                Gracias por su interés en nuestros programas de posgrado. Queremos compartir con usted
                                información detallada sobre el programa:
                              </p>
                              <h2 style="margin:0 0 16px 0; color:#1a3c6e; font-size:22px; line-height:1.4;">
                                %s
                              </h2>
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                Este programa está diseñado para profesionales que desean fortalecer sus competencias,
                                actualizarse en las últimas tendencias de su área y acceder a mejores oportunidades
                                laborales, tanto a nivel nacional como internacional.
                              </p>
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                En la malla curricular adjunta encontrará el detalle de los módulos, las asignaturas
                                principales y la duración estimada del programa, así como los enfoques metodológicos
                                y los resultados de aprendizaje esperados.
                              </p>
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                Algunos de los beneficios de este programa son:
                              </p>
                              <ul style="margin:0 0 16px 20px; padding:0; font-size:14px; line-height:1.6;">
                                <li>Docentes con amplia experiencia académica y profesional.</li>
                                <li>Enfoque práctico orientado a la solución de problemas reales.</li>
                                <li>Red de contactos con otros profesionales y organizaciones.</li>
                                <li>Acceso a recursos académicos y acompañamiento permanente.</li>
                              </ul>
                              <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                                Si desea recibir una asesoría personalizada sobre requisitos, proceso de admisión,
                                descuentos o facilidades de pago, puede responder a este correo o contactarnos por
                                WhatsApp utilizando el botón que encontrará más abajo.
                              </p>
                              <p style="margin:0 0 0 0; font-size:14px; line-height:1.6;">
                                Será un gusto acompañarle en su proceso de formación de posgrado.
                              </p>
                            </td>
                          </tr>

                          <!-- Imagen del programa (subjectImage) -->
                          <tr>
                            <td align="center" style="padding:0 24px 24px 24px;">
                              <img src="cid:subjectImage" alt="Imagen del programa" style="display:block; max-width:600px; width:100%%; border:0;"/>
                            </td>
                          </tr>

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
                """.formatted(recipientName, programName);
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
