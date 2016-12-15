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
public class BuildMatcherProcessor extends AbstractProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, BuildMatcherData> dataByDestination = new HashMap<>();
        roundEnv.getElementsAnnotatedWith(BuildMatcher.class).stream()
                .filter(element -> element instanceof TypeElement)
                .filter(element -> element.getAnnotation(Generated.class) == null)
                .forEach(element -> {
                    TypeElement typeElement = (TypeElement) element;

                    AnnotationMirror mirror = getAnnotationMirror(typeElement, BuildMatcher.class);

                    processBuildMatcherAnnotation(dataByDestination, element, mirror);
                });

        roundEnv.getElementsAnnotatedWith(BuildMatchers.class).stream()
                .filter(element -> element instanceof TypeElement)
                .filter(element -> element.getAnnotation(Generated.class) == null)
                .forEach(element -> {
                    TypeElement typeElement = (TypeElement) element;
                    AnnotationMirror buildMatchers = getAnnotationMirror(typeElement, BuildMatchers.class);
                    Optional.ofNullable(getAnnotationValue(buildMatchers, "value"))
                            .map(AnnotationValue::getValue)
                            .map(value -> (List<AnnotationMirror>) value)
                            .orElse(Collections.emptyList())
                            .forEach(child -> processBuildMatcherAnnotation(dataByDestination, typeElement, child));
                });

        TypeElement objectElement = processingEnv.getElementUtils().getTypeElement(Object.class.getCanonicalName());
        for (BuildMatcherData settings : dataByDestination.values()) {
            TypeElement destinationElement = settings.pojoElement;
            MatcherPojo matcherPojo = new MatcherPojo(
                    destinationElement.getQualifiedName().toString(),
                    ClassNameUtility.extractClass(destinationElement.getQualifiedName().toString()),
                    settings.destinationPackage,
                    settings.destinationName);

            processingEnv.getElementUtils().getAllMembers(destinationElement).stream()
                    .filter(enclosed -> enclosed instanceof ExecutableElement)
                    .map(enclosed -> (ExecutableElement) enclosed)
                    .filter(executableElement -> !settings.skipObjectMethods
                            || !executableElement.getEnclosingElement().equals(objectElement))
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
                                matcherPojo.getPojoName(),
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
                        .filter(executableElement ->
                                instanceOf(destinationElement, executableElement.getParameters().get(0).asType()))
                        .filter(executableElement -> executableElement.getModifiers().contains(Modifier.PUBLIC))
                        .filter(executableElement -> executableElement.getModifiers().contains(Modifier.STATIC))
                        .forEach(executableElement -> {
                            String returnType = getReturnTypeAsString(executableElement);

                            String methodName = executableElement.getSimpleName().toString();
                            matcherPojo.addUtility(new WithUtilityPojo(
                                    matcherPojo.getPojoName(),
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
                        filer.createSourceFile(settings.destinationPackage + "." + settings.destinationName);
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
            Map<String, BuildMatcherData> candidates, Element element, AnnotationMirror mirror) {
        try {
            TypeElement pojo = Optional.ofNullable(getTypeElement(getAnnotationValue(mirror, "pojo")))
                    .orElse(getTypeElement(getAnnotationValue(mirror, "value")));
            if (pojo == null) {
                throw new ProcessingException(Diagnostic.Kind.ERROR, "@BuildMatcher pojo() or value() must be set.");
            }

            String qualifiedClassName = pojo.getQualifiedName().toString();
            String destinationPackage = Optional.ofNullable(getAnnotationValue(mirror, "destinationPackage"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (String) v)
                    .orElse(ClassNameUtility.extractPackage(qualifiedClassName));

            String destinationName = Optional.ofNullable(getAnnotationValue(mirror, "destinationName"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (String) v)
                    .orElse(ClassNameUtility.extractClass(qualifiedClassName) + "Matcher");

            boolean skipObjectMethods = Optional.ofNullable(getAnnotationValue(mirror, "skipObjectMethods"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (boolean) v)
                    .orElse(false);

            String fullDestination = destinationPackage + "." + destinationName;

            TypeElement pojoElement = processingEnv.getElementUtils().getTypeElement(qualifiedClassName);

            List<String> utilities = qualifiedNames(getAnnotationValue(mirror, "utilities"));

            BuildMatcherData settings = new BuildMatcherData(
                    pojoElement, destinationPackage, destinationName, skipObjectMethods, utilities);
            BuildMatcherData previous = candidates.get(fullDestination);
            if (previous != null && !settings.pojoElement.equals(previous.pojoElement)) {
                throw Exceptions.formatMessage(
                        message -> new ProcessingException(Diagnostic.Kind.ERROR, message),
                        "BuildMatcher with destination {} already defined, but with different type {}.",
                        fullDestination,
                        pojoElement.getQualifiedName());
            }
            candidates.compute(fullDestination, (c, old) -> merge(settings, old));
        } catch (ProcessingException e) {
            processingEnv.getMessager().printMessage(e.getKind(), e.getMessage(), element);
        }
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

    private BuildMatcherData merge(BuildMatcherData settings, BuildMatcherData old) {
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

    private TypeElement getTypeElement(AnnotationValue annotationValue) {
        return Optional.ofNullable(annotationValue)
                .map(AnnotationValue::getValue)
                .map(value -> (TypeMirror) value)
                .map(mirror -> (TypeElement) processingEnv.getTypeUtils().asElement(mirror))
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

    private boolean instanceOf(TypeElement candidate, TypeMirror expected) {
        TypeElement expectedType = (TypeElement) processingEnv.getTypeUtils().asElement(expected);
        TypeElement typeInHierarchy = candidate;
        do {
            if (typeInHierarchy.equals(expectedType)) {
                return true;
            }
            typeInHierarchy = (TypeElement) processingEnv.getTypeUtils().asElement(typeInHierarchy.getSuperclass());
        } while (!typeInHierarchy.getQualifiedName().toString().equals(Object.class.getCanonicalName()));
        return false;
    }

    private static class BuildMatcherData {
        private final String destinationPackage;
        private final String destinationName;
        private final TypeElement pojoElement;
        private final boolean skipObjectMethods;
        private final Set<String> utilities = new HashSet<>();

        private BuildMatcherData(
                TypeElement pojoElement,
                String destinationPackage,
                String destinationName,
                boolean skipObjectMethods,
                Collection<String> utilities) {
            this.pojoElement = pojoElement;
            this.destinationPackage = destinationPackage;
            this.destinationName = destinationName;
            this.skipObjectMethods = skipObjectMethods;
            addUtilities(utilities);
        }

        private void addUtilities(Collection<String> utilities) {
            this.utilities.addAll(utilities);
        }
    }
}
