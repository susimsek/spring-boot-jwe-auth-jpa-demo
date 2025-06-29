package io.github.susimsek.springbootjweauthjpademo.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.exception.RateLimitExceededException;
import io.github.susimsek.springbootjweauthjpademo.config.spel.SpelResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final ProxyManager<String> proxyManager;
    private final ApplicationProperties props;
    private final SpelResolver spelResolver;

    @Around("@annotation(rateLimiter)")
    public Object applyRateLimit(ProceedingJoinPoint pjp, RateLimiter rateLimiter) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Object[] args = pjp.getArgs();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String name = rateLimiter.name();
        String bucketPrefix = "bucket:";
        String keyExpr  = rateLimiter.key();
        String bucketKey;
        if ("#ip".equals(keyExpr)) {
            String ip = "";
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ip = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                    .map(h -> h.split(",")[0].trim())
                    .orElseGet(req::getRemoteAddr);
            }
            bucketKey = bucketPrefix + name + ":" + ip;
        }  else if (StringUtils.hasText(keyExpr)) {
            String resolved = spelResolver.resolve(method, args, keyExpr);
            bucketKey = bucketPrefix + name + ":" + resolved;
        } else {
            if (ObjectUtils.isEmpty(args)) {
                bucketKey = bucketPrefix + name;
            } else {
                String generatedKey = Arrays.deepToString(args);
                bucketKey = bucketPrefix + name + ":" + generatedKey;
            }
        }
        ApplicationProperties.RateLimiter.Config cfg = props.getRateLimiter()
            .getInstances().getOrDefault(name, props.getRateLimiter().getDefaultConfig());
        BucketConfiguration config = BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(cfg.getLimitForPeriod())
                .refillIntervally(cfg.getLimitForPeriod(), cfg.getLimitRefreshPeriod())
                .build())
            .build();
        Bucket bucket = proxyManager.getProxy(bucketKey, () -> config);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            if (attrs != null) {
                HttpServletResponse response = attrs.getResponse();
                if (response != null) {
                    response.setHeader("X-Rate-Limit-Limit", String.valueOf(cfg.getLimitForPeriod()));
                    response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                }
            }
            return pjp.proceed();
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            throw new RateLimitExceededException(
                bucketKey, cfg.getLimitForPeriod(), probe.getRemainingTokens(), waitForRefill
            );
        }
    }
}
