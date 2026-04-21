package com.kinforgework.cplaneta.integration.email.template;

public abstract class EmailBodyDecorator implements EmailBodyComponent {

    protected final EmailBodyComponent delegate;
    protected final String content;

    protected EmailBodyDecorator(EmailBodyComponent delegate, String content) {
        this.delegate = delegate;
        this.content = content;
    }

    protected String paragraph(String content) {
        return """
                <p style="margin:0 0 12px 0; font-size:14px; line-height:1.6;">
                  %s
                </p>
                """.formatted(content);
    }
}
