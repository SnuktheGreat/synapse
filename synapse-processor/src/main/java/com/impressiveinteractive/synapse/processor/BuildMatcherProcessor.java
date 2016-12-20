package com.impressiveinteractive.synapse.processor;

import com.impressiveinteractive.synapse.exception.Exceptions;
import com.impressiveinteractive.synapse.processor.generator.MatcherGenerator;
import com.impressiveinteractive.synapse.processor.pojo.GenericPojo;
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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    String defaultDestinationPackage =
                            Optional.ofNullable(getAnnotationValue(buildMatchers, "defaultDestinationPackage"))
                                    .map(AnnotationValue::getValue)
                                    .map(v -> (String) v)
                                    .orElse(null);

                    Stream.of(
                            Optional.ofNullable(getAnnotationValue(buildMatchers, "matchers")),
                            Optional.ofNullable(getAnnotationValue(buildMatchers, "value")))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .map(AnnotationValue::getValue)
                            .map(value -> (List<AnnotationMirror>) value)
                            .orElse(Collections.emptyList())
                            .forEach(child -> processBuildMatcherAnnotation(
                                    dataByDestination, typeElement, child, defaultDestinationPackage));
                });

        TypeElement objectElement = processingEnv.getElementUtils().getTypeElement(Object.class.getCanonicalName());
        for (BuildMatcherData settings : dataByDestination.values()) {
            TypeElement destinationElement = settings.pojoElement;
            MatcherPojo matcherPojo = settings.matcherPojo;

            processingEnv.getElementUtils().getAllMembers(destinationElement).stream()
                    .filter(enclosed -> enclosed instanceof ExecutableElement)
                    .map(enclosed -> (ExecutableElement) enclosed)
                    .filter(executableElement -> settings.includeObjectMethods
                            || !executableElement.getEnclosingElement().equals(objectElement))
                    .filter(executableElement -> executableElement.getKind() == ElementKind.METHOD)
                    .filter(executableElement -> executableElement.getReturnType().getKind() != TypeKind.VOID)
                    .filter(executableElement -> executableElement.getParameters().isEmpty())
                    .filter(executableElement -> executableElement.getModifiers().contains(Modifier.PUBLIC))
                    .filter(executableElement -> !executableElement.getModifiers().contains(Modifier.STATIC))
                    .forEach(executableElement -> {
                        String returnType = getReturnTypeAsString(executableElement);
                        String simplifiedName;
                        if (settings.shortenGetterNames) {
                            simplifiedName = executableElement.getSimpleName().toString()
                                    .replaceAll("^(get|is)([A-Z].*$)", "$2");
                        } else {
                            simplifiedName = executableElement.getSimpleName().toString();
                        }

                        List<GenericPojo> generics = executableElement.getTypeParameters().stream()
                                .map(TypeParameterElement::asType)
                                .map(type -> (TypeVariable) type)
                                .map(this::toGenericPojo)
                                .collect(Collectors.toList());

                        matcherPojo.addGetterLike(new WithGetterLikePojo(
                                executableElement.getSimpleName().toString(),
                                generics,
                                simplifiedName.substring(0, 1).toUpperCase() + simplifiedName.substring(1),
                                returnType, !executableElement.getThrownTypes().isEmpty()));
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
                                    utilityElement.getQualifiedName().toString(),
                                    methodName,
                                    methodName.substring(0, 1).toUpperCase() + methodName.substring(1),
                                    returnType));
                        });
            }

            try {
                Filer filer = processingEnv.getFiler();
                JavaFileObject sourceFile = filer.createSourceFile(
                        matcherPojo.getDestinationPackage() + "." + matcherPojo.getDestinationName());
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
            Map<String, BuildMatcherData> candidates,
            Element element,
            AnnotationMirror mirror) {
        processBuildMatcherAnnotation(candidates, element, mirror, null);
    }

    private void processBuildMatcherAnnotation(
            Map<String, BuildMatcherData> candidates,
            Element element,
            AnnotationMirror mirror,
            String defaultDestinationPackage) {
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
                    .orElse(defaultDestinationPackage != null ? defaultDestinationPackage
                            : ClassNameUtility.extractPackage(qualifiedClassName));

            String pojoSimpleName = pojo.getSimpleName().toString();
            String pojoClassName = ClassNameUtility.extractClass(qualifiedClassName);
            String destinationName = Optional.ofNullable(getAnnotationValue(mirror, "destinationName"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (String) v)
                    .orElse(pojoSimpleName + "Matcher");

            String staticMethodName = Optional.ofNullable(getAnnotationValue(mirror, "staticMethodName"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (String) v)
                    .orElse(pojoSimpleName.substring(0, 1).toLowerCase() + pojoSimpleName.substring(1));

            boolean includeObjectMethods = Optional.ofNullable(getAnnotationValue(mirror, "includeObjectMethods"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (boolean) v)
                    .orElse(true);

            boolean shortenGetterNames = Optional.ofNullable(getAnnotationValue(mirror, "shortenGetterNames"))
                    .map(AnnotationValue::getValue)
                    .map(v -> (boolean) v)
                    .orElse(true);

            String fullDestination = destinationPackage + "." + destinationName;

            TypeElement pojoElement = processingEnv.getElementUtils().getTypeElement(qualifiedClassName);

            List<String> utilities = qualifiedNames(getAnnotationValue(mirror, "utilities"));

            List<GenericPojo> generics = pojoElement.getTypeParameters().stream()
                    .map(TypeParameterElement::asType)
                    .map(type -> (TypeVariable) type)
                    .map(this::toGenericPojo)
                    .collect(Collectors.toList());

            MatcherPojo matcherPojo = new MatcherPojo(qualifiedClassName, pojoSimpleName, generics,
                    destinationPackage, destinationName, staticMethodName);
            BuildMatcherData settings =
                    new BuildMatcherData(pojoElement, matcherPojo, includeObjectMethods, shortenGetterNames, utilities);
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

    private GenericPojo toGenericPojo(TypeVariable typeVar) {
        String name = typeVar.toString();
        TypeMirror upperBound = typeVar.getUpperBound();

        String upperBoundName = null;
        String upperBoundType = null;
        Element element = processingEnv.getTypeUtils().asElement(upperBound);
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            if (!typeElement.getQualifiedName().toString().equals(Object.class.getCanonicalName())) {
                upperBoundName = typeElement.getSimpleName().toString();
                upperBoundType = typeElement.getQualifiedName().toString();
            }
        } else {
            upperBoundName = upperBound.toString();
        }
        return new GenericPojo(name, upperBoundName, upperBoundType);
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
        private final MatcherPojo matcherPojo;
        private final TypeElement pojoElement;
        private final boolean includeObjectMethods;
        private final boolean shortenGetterNames;
        private final Set<String> utilities = new HashSet<>();

        private BuildMatcherData(
                TypeElement pojoElement,
                MatcherPojo matcherPojo,
                boolean includeObjectMethods,
                boolean shortenGetterNames,
                Collection<String> utilities) {
            this.matcherPojo = matcherPojo;
            this.pojoElement = pojoElement;
            this.includeObjectMethods = includeObjectMethods;
            this.shortenGetterNames = shortenGetterNames;
            addUtilities(utilities);
        }

        private void addUtilities(Collection<String> utilities) {
            this.utilities.addAll(utilities);
        }
    }
}
