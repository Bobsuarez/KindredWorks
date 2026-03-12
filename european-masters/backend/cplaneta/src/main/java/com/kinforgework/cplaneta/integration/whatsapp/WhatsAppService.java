package com.kinforgework.cplaneta.integration.whatsapp;

/**
 * Abstraction for the WhatsApp messaging channel.
 * Implement this interface to integrate with any WhatsApp Business API provider
 * (Meta Cloud API, Twilio, MessageBird, etc.) without coupling business logic
 * to a specific vendor.
 */
public interface WhatsAppService {

    /**
     * Sends a text message to the given phone number via WhatsApp.
     *
     * @param phoneNumber Recipient's phone number in E.164 format (e.g. +573001234567)
     * @param message     Plain text message body
     */
    void sendTextMessage(String phoneNumber, String message);
}
