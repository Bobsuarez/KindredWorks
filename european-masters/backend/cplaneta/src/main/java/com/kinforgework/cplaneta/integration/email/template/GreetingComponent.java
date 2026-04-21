package com.kinforgework.cplaneta.integration.email.template;

public class GreetingComponent implements EmailBodyComponent {
    @Override
    public String render(String recipientName, String programName) {
        return """
                <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                  Hola <strong>%s</strong>,
                </p>
                <h2 style="margin:0 0 16px 0; color:#1a3c6e; font-size:22px; line-height:1.4;">
                  %s
                </h2>
                """.formatted(recipientName, programName);
    }
}
