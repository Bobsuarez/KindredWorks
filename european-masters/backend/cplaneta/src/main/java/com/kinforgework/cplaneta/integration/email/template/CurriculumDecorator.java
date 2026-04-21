package com.kinforgework.cplaneta.integration.email.template;

public class CurriculumDecorator extends EmailBodyDecorator {
    public CurriculumDecorator(EmailBodyComponent delegate, String content) {
        super(delegate, content);
    }

    @Override
    public String render(String recipientName, String programName) {
        return delegate.render(recipientName, programName)
                + paragraph(content);
    }
}
