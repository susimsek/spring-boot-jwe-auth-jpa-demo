package io.github.susimsek.springbootjweauthjpademo.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ClassScanner {

    /**
     * Scan and return all classes under the given base package.
     */
    public Set<Class<?>> scanPackage(String basePackage) {
        return scanAllClasses(basePackage);
    }

    /**
     * Scan for all subtypes of superType under the given base package.
     */
    public Set<Class<?>> scanForSubTypes(String basePackage, Class<?> superType) {
        return scanAllClasses(basePackage).stream()
            .filter(cls -> superType.isAssignableFrom(cls) && !cls.equals(superType))
            .collect(Collectors.toSet());
    }

    /**
     * Scan for all classes annotated with annotationClass under the given base package.
     */
    public Set<Class<?>> scanForAnnotatedClasses(String basePackage, Class<? extends Annotation> annotationClass) {
        return scanAllClasses(basePackage).stream()
            .filter(cls -> cls.isAnnotationPresent(annotationClass))
            .collect(Collectors.toSet());
    }

    /**
     * Scan for classes referenced in the 'value' attribute of the given annotation under basePackage.
     */
    public Set<Class<?>> scanForValueAnnotatedClasses(String basePackage, Class<? extends Annotation> annotationClass) {
        Set<Class<?>> annotated = scanForAnnotatedClasses(basePackage, annotationClass);
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> cls : annotated) {
            Annotation ann = cls.getAnnotation(annotationClass);
            try {
                Method m = annotationClass.getMethod("value");
                Object v = m.invoke(ann);
                if (v instanceof Class) {
                    result.add((Class<?>) v);
                } else if (v instanceof Class[]) {
                    Collections.addAll(result, (Class<?>[]) v);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to extract value from annotation " + annotationClass, e);
            }
        }
        return result;
    }

    /**
     * Core scanning of .class resources under the package, converting them to Class objects.
     */
    private Set<Class<?>> scanAllClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
            Resource[] resources = resolver.getResources("classpath*:" + path);
            for (Resource resource : resources) {
                if (!resource.isReadable()) continue;
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                classes.add(Class.forName(reader.getClassMetadata().getClassName()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan package " + basePackage, e);
        }
        return classes;
    }
}
