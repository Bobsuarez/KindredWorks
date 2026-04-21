package com.kinforgework.cplaneta.integration.email.template;

public interface EmailBodyComponent {
    String render(String recipientName, String programName);
}
