package com.kinforgework.cplaneta.integration.email;

import java.io.File;

/**
 * Abstraction for the email sending channel.
 * Implement this interface to swap providers (Jakarta Mail, SendGrid, AWS SES, etc.)
 * without touching business logic.
 */
public interface EmailService {

    /**
     * Sends a promotional email to a contact.
     *
     * @param to            Recipient email address
     * @param recipientName Recipient display name (used for personalisation)
     * @param programName   Name of the master's degree program
     * @param subjectImage  File pointing to the subject/banner image
     * @param curriculumPdf File pointing to the PDF curriculum attachment
     */
    void sendPromotionalEmail(
            String to,
            String recipientName,
            String programName,
            File subjectImage,
            File curriculumPdf
    );
}
