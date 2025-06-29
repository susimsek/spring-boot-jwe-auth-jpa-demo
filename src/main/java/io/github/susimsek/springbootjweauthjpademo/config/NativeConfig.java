package io.github.susimsek.springbootjweauthjpademo.config;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiterAspect;
import io.github.susimsek.springbootjweauthjpademo.exception.Violation;
import io.github.susimsek.springbootjweauthjpademo.util.ClassScanner;
import jakarta.persistence.IdClass;
import jakarta.persistence.metamodel.StaticMetamodel;
import jakarta.validation.ConstraintValidator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.Set;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(NativeConfig.class)
public class NativeConfig implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
            .registerType(sun.misc.Unsafe.class, hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        hints.reflection()
            .registerType(java.util.Locale.class, hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        hints.resources().registerPattern("i18n/*");

        hints.reflection()
            .registerType(HashMap.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
            );

        hints.reflection()
            .registerType(Jwt.class,
                hint -> hint.withMembers(
                    MemberCategory.INVOKE_PUBLIC_METHODS
                )
            );

        hints.reflection()
            .registerType(RateLimiterAspect.class, hint ->
                hint.withMembers(
                    MemberCategory.INVOKE_DECLARED_METHODS
                )
            );


        hints.reflection()
            .registerType(Violation.class, hint -> hint
                .withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS
                )
            );

        registerHibernateHints(hints);
        registerLiquibaseHints(hints);
        registerValidatorHints(hints);
        registerFilterHints(hints);
    }

    private void registerHibernateHints(RuntimeHints hints) {
        hints.reflection()
            .registerType(org.hibernate.cache.jcache.internal.JCacheRegionFactory.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            );
        hints.reflection()
            .registerType(javax.cache.configuration.MutableConfiguration.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            );
        hints
            .reflection()
            .registerType(org.hibernate.binder.internal.BatchSizeBinder.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            );

        Set<Class<?>> idClasses = ClassScanner.scanForValueAnnotatedClasses(
            "io.github.susimsek.springbootjweauthjpademo.domain",
            IdClass.class
        );
        for (Class<?> idClazz : idClasses) {
            hints.reflection()
                .registerType(idClazz, hint -> hint.withMembers(
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.DECLARED_FIELDS
                ));
        }

        Set<Class<?>> metamodels = ClassScanner.scanForAnnotatedClasses(
            "io.github.susimsek.springbootjweauthjpademo.domain",
            StaticMetamodel.class
        );
        for (Class<?> meta : metamodels) {
            hints.reflection()
                .registerType(meta, hint -> hint.withMembers(MemberCategory.DECLARED_FIELDS));
        }
    }

    private void registerLiquibaseHints(RuntimeHints hints) {
        hints.resources().registerPattern("config/liquibase/*");
        hints
            .reflection()
            .registerType(liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            );
        hints
            .reflection()
            .registerType(liquibase.ui.LoggerUIService.class,
                hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        hints
            .reflection()
            .registerType(liquibase.database.LiquibaseTableNamesFactory.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            );
        hints
            .reflection()
            .registerType(liquibase.report.ShowSummaryGeneratorFactory.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            );
        hints
            .reflection()
            .registerType(liquibase.changelog.FastCheckService.class, hint ->
                hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            );
    }

    private void registerValidatorHints(RuntimeHints hints) {
        Set<Class<?>> validators = ClassScanner.scanForSubTypes(
            "io.github.susimsek.springbootjweauthjpademo.validation",
            ConstraintValidator.class
        );
        for (Class<?> validatorClass : validators) {
            hints.reflection()
                .registerType(validatorClass, hint ->
                    hint.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
                );
        }
    }

    private void registerFilterHints(RuntimeHints hints) {
        Set<Class<?>> filterClasses = ClassScanner.scanPackage(
            "io.github.susimsek.springbootjweauthjpademo.dto.filter");
        for (Class<?> filterClass : filterClasses) {
            hints.reflection()
                .registerType(filterClass, hint -> hint
                    .withMembers(
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    )
                );
        }
    }
}
