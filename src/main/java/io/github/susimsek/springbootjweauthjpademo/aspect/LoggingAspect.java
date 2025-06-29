package io.github.susimsek.springbootjweauthjpademo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;

@Aspect
@Slf4j
public class LoggingAspect {

    @Pointcut(
        "within(@org.springframework.stereotype.Repository *)" +
        " || within(@org.springframework.stereotype.Service *)" +
        " || within(@org.springframework.web.bind.annotation.RestController *)"
    )
    public void springBeanPointcut() {
        // pointcut for Spring beans
    }

    @Pointcut(
        "within(io.github.susimsek.springbootjweauthjpademo.repository..*)" +
        " || within(io.github.susimsek.springbootjweauthjpademo.service..*)" +
        " || within(io.github.susimsek.springbootjweauthjpademo.controller..*)"
    )
    public void applicationPackagePointcut() {
        // pointcut for application-specific packages
    }

    /**
     * Combined pointcut: beans in application packages
     */
    @Pointcut("springBeanPointcut() && applicationPackagePointcut()")
    public void loggingPointcut() {
        // combined pointcut
    }

    @Around("loggingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        if (log.isDebugEnabled()) {
            log.debug("Enter {}.{}() with arguments = {}", className, methodName, Arrays.toString(args));
        }

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        if (log.isDebugEnabled()) {
            log.debug("Exit {}.{}() with result = {} ({} ms)", className, methodName, result, duration);
        }
        return result;
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        log.error("Exception in {}.{}() with cause = {}", className, methodName, cause, ex);
    }
}
