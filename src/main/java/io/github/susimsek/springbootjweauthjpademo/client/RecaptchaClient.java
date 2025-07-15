package io.github.susimsek.springbootjweauthjpademo.client;

import io.github.susimsek.springbootjweauthjpademo.dto.response.RecaptchaDTO;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;


@HttpExchange
public interface RecaptchaClient {

    @PostExchange(
        contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        accept = MediaType.APPLICATION_JSON_VALUE
    )
    RecaptchaDTO verify(@RequestBody MultiValueMap<String,String> request);
}
