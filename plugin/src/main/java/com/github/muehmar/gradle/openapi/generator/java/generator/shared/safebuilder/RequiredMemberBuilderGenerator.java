package com.github.muehmar.gradle.openapi.generator.java.generator.shared.safebuilder;

import static io.github.muehmar.codegenerator.Generator.constant;
import static io.github.muehmar.codegenerator.java.JavaModifier.FINAL;
import static io.github.muehmar.codegenerator.java.JavaModifier.PUBLIC;
import static io.github.muehmar.codegenerator.java.JavaModifier.STATIC;

import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.java.JavaRefs;
import com.github.muehmar.gradle.openapi.generator.java.generator.shared.JavaDocGenerator;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMember;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaObjectPojo;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.java.ClassGenBuilder;
import io.github.muehmar.codegenerator.java.MethodGenBuilder;
import lombok.Value;

class RequiredMemberBuilderGenerator {
  private RequiredMemberBuilderGenerator() {}

  public static Generator<JavaObjectPojo, PojoSettings> generator() {
    return Generator.<JavaObjectPojo, PojoSettings>emptyGen()
        .appendList(requiredBuilder(), RequiredMember::fromObjectPojo, Generator.newLine());
  }

  private static Generator<RequiredMember, PojoSettings> requiredBuilder() {
    return ClassGenBuilder.<RequiredMember, PojoSettings>create()
        .clazz()
        .nested()
        .packageGen(Generator.emptyGen())
        .noJavaDoc()
        .noAnnotations()
        .modifiers(STATIC, FINAL, PUBLIC)
        .className(RequiredMember::builderClassName)
        .noSuperClass()
        .noInterfaces()
        .content(requiredBuilderContent())
        .build();
  }

  private static Generator<RequiredMember, PojoSettings> requiredBuilderContent() {
    final Generator<RequiredMember, PojoSettings> normalSetter = requiredBuilderSetter("%s %s");
    final Generator<RequiredMember, PojoSettings> nullableSetter =
        requiredBuilderSetter("Optional<%s> %s").append(w -> w.ref(JavaRefs.JAVA_UTIL_OPTIONAL));
    return Generator.<RequiredMember, PojoSettings>emptyGen()
        .append(constant("private final Builder builder;"))
        .appendNewLine()
        .append((m, s, w) -> w.println("private Builder%d(Builder builder) {", m.idx))
        .append(constant("this.builder = builder;"), 1)
        .append(constant("}"))
        .appendNewLine()
        .append(normalSetter)
        .appendSingleBlankLine()
        .append(nullableSetter.filter(RequiredMember::isNullable));
  }

  private static Generator<RequiredMember, PojoSettings> requiredBuilderSetter(
      String argumentFormat) {
    final Generator<RequiredMember, PojoSettings> method =
        MethodGenBuilder.<RequiredMember, PojoSettings>create()
            .modifiers(PUBLIC)
            .noGenericTypes()
            .returnType(RequiredMember::nextBuilderClassName)
            .methodName(
                (m, s) -> m.member.prefixedMethodName(s.getBuilderMethodPrefix()).asString())
            .singleArgument(
                m ->
                    String.format(
                        argumentFormat,
                        m.member.getJavaType().getFullClassName().asString(),
                        m.member.getName()))
            .content(
                (m, s, w) ->
                    w.println(
                        "return new %s(builder.%s(%s));",
                        m.nextBuilderClassName(),
                        m.member.prefixedMethodName(s.getBuilderMethodPrefix()),
                        m.member.getName()))
            .build();
    return JavaDocGenerator.<PojoSettings>javaDoc()
        .<RequiredMember>contraMap(m -> m.member.getDescription())
        .append(method);
  }

  @Value
  private static class RequiredMember {
    JavaPojoMember member;
    int idx;

    private static PList<RequiredMember> fromObjectPojo(JavaObjectPojo pojo) {
      return pojo.getMembers()
          .filter(JavaPojoMember::isRequired)
          .zipWithIndex()
          .map(p -> new RequiredMember(p.first(), p.second()));
    }

    private String builderClassName() {
      return String.format("Builder%d", idx);
    }

    private String nextBuilderClassName() {
      return String.format("Builder%d", idx + 1);
    }

    public boolean isNullable() {
      return member.isNullable();
    }
  }
}
