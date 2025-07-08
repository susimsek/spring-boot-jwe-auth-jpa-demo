package io.github.susimsek.springbootjweauthjpademo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageLookupService {

    private final MessageService messageService;

    public Optional<String> findByCodeAndLocale(String code, Locale locale) {
        return Optional.ofNullable(messageService.getMessages(locale).get(code))
            .or(() -> Optional.ofNullable(
                messageService.getMessages(Locale.US).get(code)
            ));
    }

    @Scheduled(cron = "0 0/30 * * * *")
    public void refreshMessages() {
        messageService.refreshMessages(Locale.US);
    }
}
