package com.emamagic.generator;

import com.emamagic.util.PageData;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public final class NavigationGenerator {
    private NavigationGenerator() {
    }

    public static void generateNavigatorClass(ProcessingEnvironment processingEnv, Map<String, PageData> pageParamMap) {
        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T($S)", RuntimeException.class, "You cannot create an instance of Navigator")
                        .build());

        for (Map.Entry<String, PageData> entry : pageParamMap.entrySet()) {
            String pageName = entry.getKey();
            PageData pageData = entry.getValue();

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("navTo" + pageName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            methodBuilder.addStatement("$T page = new $T()",
                    ClassName.bestGuess(pageData.qualifiedName()),
                    ClassName.bestGuess(pageData.qualifiedName()));

            for (VariableElement param : pageData.params()) {
                methodBuilder.addParameter(TypeName.get(param.asType()), param.getSimpleName().toString());

                methodBuilder.addStatement("try { $T field = page.getClass().getDeclaredField($S); field.setAccessible(true); field.set(page, $L); } catch (Exception e) { throw new RuntimeException(e); }",
                        Field.class, param.getSimpleName().toString(), param.getSimpleName().toString());
            }

            methodBuilder.addStatement("page.display()");
            navigatorClass.addMethod(methodBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder("com.emamagic.navigator", navigatorClass.build())
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException ignored) {
            // Handle exception if necessary
        }
    }
}
