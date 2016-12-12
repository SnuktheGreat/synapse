package com.impressiveinteractive.synapse.processor;

import com.impressiveinteractive.synapse.exception.Exceptions;
import com.impressiveinteractive.synapse.processor.generator.MatcherGenerator;
import com.impressiveinteractive.synapse.processor.pojo.MatcherPojo;
import com.impressiveinteractive.synapse.processor.pojo.WithGetterLikePojo;
import com.impressiveinteractive.synapse.processor.pojo.WithUtilityPojo;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * This processor is used to generate Hamcrest matchers from {@link BuildMatchers} and {@link BuildMatcher} annotations.
 */
@SupportedAnnotationTypes({
        "com.impressiveinteractive.synapse.processor.BuildMatchers",
        "com.impressiveinteractive.synapse.processor.BuildMatcher"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SynapseProcessor extends AbstractProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, MatcherSettings> candidates = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(BuildMatcher.class)) {
            if (element instanceof TypeElement && element.getAnnotation(Generated.class) == null) {
                TypeElement typeElement = (TypeElement) element;

                AnnotationMirror mirror = getAnnotationMirror(typeElement, BuildMatcher.class);

                processBuildMatcherAnnotation(candidates, element, mirror);
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(BuildMatchers.class)) {
            if (element instanceof TypeElement && element.getAnnotation(Generated.class) == null) {
                TypeElement typeElement = (TypeElement) element;
                AnnotationMirror buildMatchers = getAnnotationMirror(typeElement, BuildMatchers.class);
                Optional.ofNullable(getAnnotationValue(buildMatchers, "value"))
                        .map(AnnotationValue::getValue)
                        .map(value -> (List<AnnotationMirror>) value)
                        .orElse(Collections.emptyList())
                        .forEach(child -> processBuildMatcherAnnotation(candidates, typeElement, child));
            }
        }

        for (MatcherSettings settings : candidates.values()) {
            TypeElement destinationElement = settings.destination;
            MatcherPojo matcherPojo = new MatcherPojo(
                    ClassNameUtility.extractPackage(destinationElement.getQualifiedName().toString()),
                    ClassNameUtility.extractClass(destinationElement.getQualifiedName().toString()));

            destinationElement.getEnclosedElements().stream()
                    .filter(enclosed -> enclosed instanceof ExecutableElement)
                    .map(enclosed -> (ExecutableElement) enclosed)
                    .filter(executableElement -> executableElement.getKind() == ElementKind.METHOD)
                    .filter(executableElement -> executableElement.getReturnType().getKind() != TypeKind.VOID)
                    .filter(executableElement -> executableElement.getParameters().isEmpty())
                    .filter(executableElement -> executableElement.getModifiers().contains(Modifier.PUBLIC))
                    .filter(executableElement -> !executableElement.getModifiers().contains(Modifier.STATIC))
                    .forEach(executableElement -> {
                        String returnType = getReturnTypeAsString(executableElement);
                        String simplifiedName = executableElement.getSimpleName().toString()
                                .replaceAll("^(get|is)([A-Z].*$)", "$2");

                        matcherPojo.addGetterLike(new WithGetterLikePojo(
                                matcherPojo.getDestinationName(),
                                executableElement.getSimpleName().toString(),
                                simplifiedName.substring(0, 1).toUpperCase() + simplifiedName.substring(1),
                                returnType));
                    });

            for (String utility : settings.utilities) {
                TypeElement utilityElement = processingEnv.getElementUtils().getTypeElement(utility);
                utilityElement.getEnclosedElements().stream()
                        .filter(enclosed -> enclosed instanceof ExecutableElement)
                        .map(enclosed -> (ExecutableElement) enclosed)
                        .filter(executableElement -> executableElement.getKind() == ElementKind.METHOD)
                        .filter(executableElement -> executableElement.getReturnType().getKind() != TypeKind.VOID)
                        .filter(executableElement -> executableElement.getParameters().size() == 1)
                        .filter(executableElement -> processingEnv.getTypeUtils().asElement(
                                executableElement.getParameters().get(0).asType()).equals(destinationElement))
                        .filter(executableElement -> executableElement.getModifiers().contains(Modifier.PUBLIC))
                        .filter(executableElement -> executableElement.getModifiers().contains(Modifier.STATIC))
                        .forEach(executableElement -> {
                            String returnType = getReturnTypeAsString(executableElement);

                            String methodName = executableElement.getSimpleName().toString();
                            matcherPojo.addUtility(new WithUtilityPojo(
                                    matcherPojo.getDestinationName(),
                                    utilityElement.getQualifiedName().toString(),
                                    methodName,
                                    methodName.substring(0, 1).toUpperCase() + methodName.substring(1),
                                    returnType));
                        });
            }

            try {
                Filer filer = processingEnv.getFiler();
                JavaFileObject sourceFile =
                        filer.createSourceFile(destinationElement.getQualifiedName() + "Matcher");
                try (Writer writer = sourceFile.openWriter()) {
                    writer.write(MatcherGenerator.generateSourceCode(matcherPojo));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to write file.", e);
            }
        }
        return true;
    }

    private void processBuildMatcherAnnotation(
            Map<String, MatcherSettings> candidates, Element element, AnnotationMirror mirror) {
        try {
            String qualifiedClassName = getQualifiedClassName(mirror);

            TypeElement destinationElement = processingEnv.getElementUtils().getTypeElement(qualifiedClassName);
            if (destinationElement.getModifiers().contains(Modifier.ABSTRACT)) {
                throw Exceptions.formatMessage(
                        message -> new ProcessingException(Diagnostic.Kind.ERROR, message),
                        "Can not use BuildMatcher with abstract type {}.",
                        destinationElement.getQualifiedName());
            }

            MatcherSettings settings = new MatcherSettings(destinationElement, getUtilities(mirror));
            candidates.compute(qualifiedClassName, (c, old) -> merge(settings, old));
        } catch (ProcessingException e) {
            processingEnv.getMessager().printMessage(e.getKind(), e.getMessage(), element);
        }
    }

    private String getQualifiedClassName(AnnotationMirror mirror) throws ProcessingException {
        String qualifiedClassName;
        String pojo = getPojo(mirror);
        String value = getValue(mirror);
        if (pojo != null) {
            qualifiedClassName = pojo;
        } else if (value != null) {
            qualifiedClassName = value;
        } else {
            throw new ProcessingException(Diagnostic.Kind.ERROR,
                    "@BuildMatcher pojo() or value() must be set.");
        }
        return qualifiedClassName;
    }

    private String getReturnTypeAsString(ExecutableElement executableElement) {
        String returnType;
        if (executableElement.getReturnType().getKind().isPrimitive()) {
            returnType = processingEnv.getTypeUtils().boxedClass(processingEnv.getTypeUtils()
                    .getPrimitiveType(executableElement.getReturnType().getKind()))
                    .getQualifiedName().toString();
        } else {
            returnType = executableElement.getReturnType().toString();
        }
        return returnType;
    }

    private MatcherSettings merge(MatcherSettings settings, MatcherSettings old) {
        if (old == null) {
            return settings;
        } else {
            old.addUtilities(settings.utilities);
            return old;
        }
    }

    private AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> cls) {
        String clazzName = cls.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        throw Exceptions.format(IllegalArgumentException::new,
                "Could not find annotation {} on {}.", cls.getCanonicalName(), typeElement);
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getValue(AnnotationMirror mirror) {
        return qualifiedName(getAnnotationValue(mirror, "value"));
    }

    private String getPojo(AnnotationMirror mirror) {
        return qualifiedName(getAnnotationValue(mirror, "pojo"));
    }

    private List<String> getUtilities(AnnotationMirror mirror) {
        return qualifiedNames(getAnnotationValue(mirror, "utilities"));
    }

    private String qualifiedName(AnnotationValue annotationValue) {
        return Optional.ofNullable(annotationValue)
                .map(AnnotationValue::getValue)
                .map(value -> (TypeMirror) value)
                .map(mirror -> (TypeElement) processingEnv.getTypeUtils().asElement(mirror))
                .map(TypeElement::getQualifiedName)
                .map(Object::toString)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private List<String> qualifiedNames(AnnotationValue annotationValue) {
        return Optional.ofNullable(annotationValue)
                .map(AnnotationValue::getValue)
                .map(value -> (List<AnnotationValue>) value)
                .orElse(Collections.emptyList())
                .stream()
                .map(AnnotationValue::getValue)
                .map(value -> (TypeMirror) value)
                .map(mirror -> (TypeElement) processingEnv.getTypeUtils().asElement(mirror))
                .map(TypeElement::getQualifiedName)
                .map(Object::toString)
                .collect(toList());
    }

    private static class MatcherSettings {
        private final TypeElement destination;
        private final Set<String> utilities = new HashSet<>();

        private MatcherSettings(TypeElement destination, Collection<String> utilities) {
            this.destination = destination;
            addUtilities(utilities);
        }

        private void addUtilities(Collection<String> utilities) {
            this.utilities.addAll(utilities);
        }
    }
}
