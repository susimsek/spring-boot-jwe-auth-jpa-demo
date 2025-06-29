package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "GreetRequest",
    description = "Payload for greeting"
)
public record GreetRequestDTO(

    @Schema(
        description = "The message that will be echoed back to the user",
        example = "Hello from the restclient!"
    )
    @NotBlank
    @Size(min = 1, max = 100)
    String message

) { }
