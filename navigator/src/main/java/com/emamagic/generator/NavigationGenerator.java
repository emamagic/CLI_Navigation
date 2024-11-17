package com.emamagic.generator;

import com.emamagic.util.PageData;
import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public final class NavigationGenerator {
    private NavigationGenerator() {
    }

    public static void generateNavigatorClass(ProcessingEnvironment processingEnv, Map<String, PageData> pageParamMap) {
        TypeSpec.Builder navigatorClass = TypeSpec.classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec.builder(
                                ParameterizedTypeName.get(ClassName.get(Deque.class), ClassName.get("com.emamagic.util", "Navigable")),
                                "stack", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>()", ArrayDeque.class)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T($S)", RuntimeException.class, "You cannot create an instance of Navigator")
                        .build());

        MethodSpec setFieldMethod = MethodSpec.methodBuilder("setField")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(Object.class, "page")
                .addParameter(String.class, "fieldName")
                .addParameter(Object.class, "value")
                .addStatement("""
                        try {
                            $T field = page.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(page, value);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }""",
                        Field.class)
                .build();

        navigatorClass.addMethod(setFieldMethod);

        for (Map.Entry<String, PageData> entry : pageParamMap.entrySet()) {
            String pageName = entry.getKey();
            PageData pageData = entry.getValue();

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("navTo" + pageName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            ClassName pageClassName = ClassName.bestGuess(pageData.qualifiedName());
            methodBuilder.addStatement("$T page = new $T()", pageClassName, pageClassName);

            for (VariableElement param : pageData.params()) {
                String paramName = param.getSimpleName().toString();
                methodBuilder.addParameter(TypeName.get(param.asType()), paramName);
                methodBuilder.addStatement("setField(page, $S, $L)", paramName, paramName);
            }

            methodBuilder.addStatement("stack.push(page)");
            methodBuilder.addStatement("page.display()");
            navigatorClass.addMethod(methodBuilder.build());
        }

        MethodSpec popMethod = MethodSpec.methodBuilder("pop")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .beginControlFlow("if (!stack.isEmpty())")
                .addStatement("stack.pop()")
                .beginControlFlow("if (!stack.isEmpty())")
                .addStatement("stack.peek().display()")
                .nextControlFlow("else")
                .addStatement("throw new $T($S)", RuntimeException.class, "page stack couldn't be empty.")
                .endControlFlow()
                .nextControlFlow("else")
                .addStatement("throw new $T($S)", RuntimeException.class, "No pages to pop.")
                .endControlFlow()
                .build();
        navigatorClass.addMethod(popMethod);

        MethodSpec popUntilMethod = MethodSpec.methodBuilder("popUntil")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.bestGuess("Navigable"))), "page")
                .beginControlFlow("while (!stack.isEmpty() && !page.isInstance(stack.peek()))")
                .addStatement("stack.pop()")
                .endControlFlow()
                .beginControlFlow("if (stack.isEmpty())")
                .addStatement("throw new $T($S + page.getSimpleName() + $S)", RuntimeException.class, "No page of type ", " found.")
                .nextControlFlow("else")
                .addStatement("stack.peek().display()")
                .endControlFlow()
                .build();
        navigatorClass.addMethod(popUntilMethod);

        JavaFile javaFile = JavaFile.builder("com.emamagic.navigator", navigatorClass.build())
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException ignored) {
            // Handle exception if necessary
        }
    }
}
