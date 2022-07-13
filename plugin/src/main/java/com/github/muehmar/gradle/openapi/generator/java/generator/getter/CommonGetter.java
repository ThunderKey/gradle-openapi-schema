package com.github.muehmar.gradle.openapi.generator.java.generator.getter;

import static io.github.muehmar.pojoextension.generator.impl.JavaModifier.PUBLIC;

import com.github.muehmar.gradle.openapi.generator.Resolver;
import com.github.muehmar.gradle.openapi.generator.data.PojoMember;
import com.github.muehmar.gradle.openapi.generator.java.JavaRefs;
import com.github.muehmar.gradle.openapi.generator.java.JavaResolver;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import io.github.muehmar.pojoextension.generator.Generator;
import io.github.muehmar.pojoextension.generator.impl.gen.MethodGenBuilder;
import java.util.function.BiFunction;

public class CommonGetter {
  private static final Resolver RESOLVER = new JavaResolver();

  private CommonGetter() {}

  public static Generator<PojoMember, PojoSettings> wrapNullableInOptionalGetterMethod() {
    return MethodGenBuilder.<PojoMember, PojoSettings>create()
        .modifiers(PUBLIC)
        .noGenericTypes()
        .returnType(f -> String.format("Optional<%s>", f.getTypeName(RESOLVER).asString()))
        .methodName(getterName())
        .noArguments()
        .content(f -> String.format("return Optional.ofNullable(%s);", f.memberName(RESOLVER)))
        .build()
        .append(w -> w.ref(JavaRefs.JAVA_UTIL_OPTIONAL));
  }

  public static Generator<PojoMember, PojoSettings> wrapNullableInOptionalGetterOrMethod() {
    return MethodGenBuilder.<PojoMember, PojoSettings>create()
        .modifiers(PUBLIC)
        .noGenericTypes()
        .returnType(f -> f.getTypeName(RESOLVER).asString())
        .methodName(f -> String.format("%sOr", f.getterName(RESOLVER)))
        .singleArgument(f -> String.format("%s defaultValue", f.getTypeName(RESOLVER).asString()))
        .content(
            f ->
                String.format(
                    "return %s == null ? defaultValue : %s;",
                    f.memberName(RESOLVER), f.memberName(RESOLVER)))
        .build()
        .append(w -> w.ref(JavaRefs.JAVA_UTIL_OPTIONAL));
  }

  public static BiFunction<PojoMember, PojoSettings, String> getterName() {
    return (field, settings) -> field.getterName(RESOLVER) + settings.suffixForField(field);
  }

  public static Generator<PojoMember, PojoSettings> nullableGetterMethodForValidation() {
    return MethodGenBuilder.<PojoMember, PojoSettings>create()
        .createModifiers((f, s) -> s.getValidationGetter().getModifier().asJavaModifiers())
        .noGenericTypes()
        .returnType(f -> f.getTypeName(RESOLVER).asString())
        .methodName((f, s) -> f.getterName(RESOLVER) + s.getValidationGetter().getSuffix())
        .noArguments()
        .content(f -> String.format("return %s;", f.memberName(RESOLVER)))
        .build();
  }
}
