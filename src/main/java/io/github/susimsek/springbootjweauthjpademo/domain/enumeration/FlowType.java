package io.github.susimsek.springbootjweauthjpademo.domain.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    enumAsRef   = true,
    example        = "LOGIN",
    description = """
        Flow type:
        * `REGISTRATION` - User registration flow
        * `LOGIN`        - User login flow
        * `MANAGEMENT`   - Administration flow
        """
)
public enum FlowType {
    REGISTRATION,
    LOGIN,
    MANAGEMENT
}
