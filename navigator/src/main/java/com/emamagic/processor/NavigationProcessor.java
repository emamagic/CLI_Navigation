package com.emamagic.processor;

import com.emamagic.annotation.Page;
import com.emamagic.annotation.Param;
import com.emamagic.generator.NavigationGenerator;
import com.emamagic.util.PageData;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@SupportedAnnotationTypes("com.emamagic.annotation.Page")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class NavigationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, PageData> pageParamMap = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(Page.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                String pageName = typeElement.getSimpleName().toString();
                String qualifiedName = typeElement.getQualifiedName().toString();

                List<VariableElement> params = new ArrayList<>();
                for (Element enclosedElement : typeElement.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getAnnotation(Param.class) != null) {
                        params.add((VariableElement) enclosedElement);
                    }
                }

                pageParamMap.put(pageName, new PageData(qualifiedName, params));
            }
        }

        NavigationGenerator.generateNavigatorClass(processingEnv, pageParamMap);
        return true;
    }

}
