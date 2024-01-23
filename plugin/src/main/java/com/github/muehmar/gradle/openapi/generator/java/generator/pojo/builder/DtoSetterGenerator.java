package com.github.muehmar.gradle.openapi.generator.java.generator.pojo.builder;

import static com.github.muehmar.gradle.openapi.util.Booleans.not;
import static io.github.muehmar.codegenerator.Generator.constant;
import static io.github.muehmar.codegenerator.Generator.newLine;
import static io.github.muehmar.codegenerator.java.JavaModifier.PRIVATE;

import ch.bluecare.commons.data.NonEmptyList;
import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.java.model.composition.JavaDiscriminator;
import com.github.muehmar.gradle.openapi.generator.java.model.member.JavaPojoMember;
import com.github.muehmar.gradle.openapi.generator.java.model.name.JavaName;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaObjectPojo;
import com.github.muehmar.gradle.openapi.generator.model.name.Name;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.java.MethodGen.Argument;
import io.github.muehmar.codegenerator.java.MethodGenBuilder;
import java.util.Optional;
import lombok.Value;

public class DtoSetterGenerator {
  private DtoSetterGenerator() {}

  public static Generator<JavaObjectPojo, PojoSettings> dtoSetterGenerator() {
    return Generator.<JavaObjectPojo, PojoSettings>emptyGen()
        .appendList(dtoSetters(), ParentPojoAndComposedPojos::fromParentPojo, newLine());
  }

  private static Generator<ParentPojoAndComposedPojos, PojoSettings> dtoSetters() {
    return Generator.<ParentPojoAndComposedPojos, PojoSettings>emptyGen()
        .appendList(singleDtoSetter(), ParentPojoAndComposedPojos::getComposedPojos, newLine());
  }

  private static Generator<ParentPojoAndComposedPojo, PojoSettings> singleDtoSetter() {
    return MethodGenBuilder.<ParentPojoAndComposedPojo, PojoSettings>create()
        .modifiers(PRIVATE)
        .noGenericTypes()
        .returnType("Builder")
        .methodName(
            (pojo, settings) ->
                pojo.prefixedClassNameForMethod(settings.getBuilderMethodPrefix()).asString())
        .singleArgument(
            pojo -> new Argument(pojo.getComposedPojo().getClassName().asString(), "dto"))
        .doesNotThrow()
        .content(dtoSetterContent())
        .build();
  }

  private static Generator<ParentPojoAndComposedPojo, PojoSettings> dtoSetterContent() {
    return Generator.<ParentPojoAndComposedPojo, PojoSettings>emptyGen()
        .appendList(
            setSingleNonDiscriminatorMember().append(setSingleDiscriminatorMember()),
            ParentPojoAndComposedPojo::getMembers)
        .append(setAdditionalProperties())
        .append(constant("return this;"));
  }

  private static Generator<PojosAndMember, PojoSettings> setSingleNonDiscriminatorMember() {
    return Generator.<PojosAndMember, PojoSettings>emptyGen()
        .append(
            (member, s, w) ->
                w.println(
                    "%s(dto.%s());",
                    member.prefixedMethodName(s.getBuilderMethodPrefix()),
                    member.getGetterNameWithSuffix(s)))
        .filter(PojosAndMember::isNotDiscriminatorMember);
  }

  private static Generator<PojosAndMember, PojoSettings> setSingleDiscriminatorMember() {
    return Generator.<PojosAndMember, PojoSettings>emptyGen()
        .append(
            (member, s, w) ->
                w.println(
                    "%s(%s);",
                    member.prefixedMethodName(s.getBuilderMethodPrefix()),
                    member.getDiscriminatorValue()))
        .filter(PojosAndMember::isDiscriminatorMember);
  }

  private static <B> Generator<ParentPojoAndComposedPojo, B> setAdditionalProperties() {
    return Generator.<ParentPojoAndComposedPojo, B>constant(
            "dto.getAdditionalProperties().forEach(this::addAdditionalProperty);")
        .filter(ppcp -> ppcp.getComposedPojo().getAdditionalProperties().isAllowed());
  }

  @Value
  private static class ParentPojoAndComposedPojos {
    JavaObjectPojo parentPojo;
    Optional<JavaDiscriminator> discriminator;
    NonEmptyList<JavaObjectPojo> composedPojos;

    private static PList<ParentPojoAndComposedPojos> fromParentPojo(JavaObjectPojo parentPojo) {
      final PList<ParentPojoAndComposedPojos> mappedAllOf =
          PList.fromOptional(
              parentPojo
                  .getAllOfComposition()
                  .map(
                      composition ->
                          new ParentPojoAndComposedPojos(
                              parentPojo, Optional.empty(), composition.getPojos())));
      final PList<ParentPojoAndComposedPojos> mappedOneOfAndAnyOf =
          parentPojo
              .getDiscriminatableCompositions()
              .map(
                  composition ->
                      new ParentPojoAndComposedPojos(
                          parentPojo, composition.getDiscriminator(), composition.getPojos()));
      return mappedAllOf.concat(mappedOneOfAndAnyOf);
    }

    public PList<ParentPojoAndComposedPojo> getComposedPojos() {
      return composedPojos
          .toPList()
          .map(
              composedPojo ->
                  new ParentPojoAndComposedPojo(parentPojo, discriminator, composedPojo));
    }
  }

  @Value
  private static class ParentPojoAndComposedPojo {
    JavaObjectPojo parentPojo;
    Optional<JavaDiscriminator> discriminator;
    JavaObjectPojo composedPojo;

    public JavaName prefixedClassNameForMethod(String prefix) {
      return composedPojo.prefixedClassNameForMethod(prefix);
    }

    private PList<PojosAndMember> getMembers() {
      return composedPojo
          .getAllMembers()
          .map(member -> new PojosAndMember(parentPojo, discriminator, composedPojo, member));
    }
  }

  @Value
  private static class PojosAndMember {
    JavaObjectPojo parentPojo;
    Optional<JavaDiscriminator> discriminator;
    JavaObjectPojo composedPojo;
    JavaPojoMember member;

    private JavaName prefixedMethodName(String prefix) {
      return member.prefixedMethodName(prefix);
    }

    public JavaName getGetterNameWithSuffix(PojoSettings settings) {
      return member.getGetterNameWithSuffix(settings);
    }

    String getDiscriminatorValue() {
      final Name schemaName = composedPojo.getSchemaName().getOriginalName();
      return discriminator
          .map(
              d ->
                  d.getValueForSchemaName(
                      schemaName,
                      strValue -> String.format("\"%s\"", strValue),
                      enumName ->
                          String.format(
                              "%s.%s",
                              member.getJavaType().getQualifiedClassName().getClassName(),
                              enumName)))
          .orElse("");
    }

    private boolean isNotDiscriminatorMember() {
      return not(isDiscriminatorMember());
    }

    private boolean isDiscriminatorMember() {
      return discriminator
          .filter(discriminator -> discriminator.getPropertyName().equals(member.getName()))
          .isPresent();
    }
  }
}
