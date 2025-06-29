package io.github.susimsek.springbootjweauthjpademo.exception;

public class MfaSetupRequiredException extends RuntimeException {
    public MfaSetupRequiredException() {
        super("MFA setup is required for this user");
    }
}
