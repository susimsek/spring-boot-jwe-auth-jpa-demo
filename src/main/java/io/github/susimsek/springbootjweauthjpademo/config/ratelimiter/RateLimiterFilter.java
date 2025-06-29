package io.github.susimsek.springbootjweauthjpademo.config.ratelimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.github.susimsek.springbootjweauthjpademo.exception.RateLimitExceededException;
import io.github.susimsek.springbootjweauthjpademo.util.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final ApplicationProperties.RateLimiter props;
    private final ProblemSupport problemSupport;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
           String ip = WebUtils.getClientIp(request);

            String bucketKey = "bucket:ip:" + ip;


            ApplicationProperties.RateLimiter.Config cfg = props.getDefaultConfig();

            BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(cfg.getLimitForPeriod())
                    .refillIntervally(cfg.getLimitForPeriod(), cfg.getLimitRefreshPeriod())
                    .build())
                .build();

            Bucket bucket = proxyManager.getProxy(bucketKey, () -> config);

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Limit", String.valueOf(cfg.getLimitForPeriod()));
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                chain.doFilter(request, response);
            } else {
                long waitSecs = probe.getNanosToWaitForRefill() / 1_000_000_000;
                problemSupport.handle(request, response, new RateLimitExceededException(
                    bucketKey,
                    cfg.getLimitForPeriod(),
                    probe.getRemainingTokens(),
                    waitSecs));
            }
        }
}
