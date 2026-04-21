package com.kinforgework.cplaneta.integration.email.template;

public class CtaDecorator extends EmailBodyDecorator {

    public CtaDecorator(EmailBodyComponent delegate, String content) {
        super(delegate, content);
    }

    @Override
    public String render(String recipientName, String programName) {
        return delegate.render(recipientName, programName)
                + paragraph(content);
    }
}
