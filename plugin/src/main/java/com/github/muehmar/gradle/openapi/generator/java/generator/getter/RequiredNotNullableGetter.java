package com.github.muehmar.gradle.openapi.generator.java.generator.getter;

import static io.github.muehmar.pojoextension.generator.impl.JavaModifier.PUBLIC;

import com.github.muehmar.gradle.openapi.generator.Resolver;
import com.github.muehmar.gradle.openapi.generator.data.PojoMember;
import com.github.muehmar.gradle.openapi.generator.java.JavaResolver;
import com.github.muehmar.gradle.openapi.generator.java.generator.RefsGenerator;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import io.github.muehmar.pojoextension.generator.Generator;
import io.github.muehmar.pojoextension.generator.impl.gen.MethodGen;

public class RequiredNotNullableGetter {
  private static final Resolver RESOLVER = new JavaResolver();

  private RequiredNotNullableGetter() {}

  public static Generator<PojoMember, PojoSettings> getter() {
    return MethodGen.<PojoMember, PojoSettings>modifiers(PUBLIC)
        .noGenericTypes()
        .returnType(f -> f.getTypeName(RESOLVER).asString())
        .methodName(f -> f.getterName(RESOLVER).asString())
        .noArguments()
        .content(f -> String.format("return %s;", f.memberName(RESOLVER)))
        .append(RefsGenerator.fieldRefs());
  }
}
