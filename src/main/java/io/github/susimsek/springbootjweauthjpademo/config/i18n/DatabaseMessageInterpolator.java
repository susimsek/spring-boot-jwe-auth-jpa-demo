package io.github.susimsek.springbootjweauthjpademo.config.i18n;

import io.github.susimsek.springbootjweauthjpademo.service.MessageLookupService;
import jakarta.validation.MessageInterpolator;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@RequiredArgsConstructor
public class DatabaseMessageInterpolator implements MessageInterpolator {

    private final MessageLookupService messageLookupService;
    private final MessageInterpolator delegate =
        new ParameterMessageInterpolator();

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        String pattern = messageLookupService.findByCodeAndLocale(messageTemplate, locale)
            .orElse(null);

        if (pattern != null) {
            return delegate.interpolate(pattern, context, locale);
        }

        return delegate.interpolate(messageTemplate, context, locale);
    }
}
