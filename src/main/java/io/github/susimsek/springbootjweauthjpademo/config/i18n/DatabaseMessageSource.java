package io.github.susimsek.springbootjweauthjpademo.config.i18n;

import io.github.susimsek.springbootjweauthjpademo.service.MessageLookupService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

@Component("messageSource")
@RequiredArgsConstructor
public class DatabaseMessageSource extends AbstractMessageSource {

    private final MessageLookupService messageLookupService;

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        return messageLookupService.findByCodeAndLocale(code, locale)
            .map(pattern -> new MessageFormat(pattern, locale))
            .orElse(null);
    }
}
