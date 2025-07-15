package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.client.RecaptchaClient;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RecaptchaDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidRecaptchaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class RecaptchaService {

    private final RecaptchaClient recaptchaClient;
    private final ApplicationProperties props;


    public void validate(String token, String remoteIp) {
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("secret",    props.getSecurity().getRecaptcha().getSecret());
        form.add("response", token);
        form.add("remoteip", remoteIp);

        RecaptchaDTO response = recaptchaClient.verify(form);

        boolean ok = response.success()
                  && response.score() >= props.getSecurity().getRecaptcha().getThreshold();

        if (!ok) {
            throw new InvalidRecaptchaException();
        }
    }
}
