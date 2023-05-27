package com.github.muehmar.gradle.openapi.generator.java.generator.pojo.getter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.muehmar.gradle.openapi.generator.java.Jakarta2ValidationRefs;
import com.github.muehmar.gradle.openapi.generator.java.JavaRefs;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMember;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers;
import com.github.muehmar.gradle.openapi.generator.model.Necessity;
import com.github.muehmar.gradle.openapi.generator.model.Nullability;
import com.github.muehmar.gradle.openapi.generator.model.constraints.Constraints;
import com.github.muehmar.gradle.openapi.generator.model.constraints.Min;
import com.github.muehmar.gradle.openapi.generator.model.type.IntegerType;
import com.github.muehmar.gradle.openapi.generator.settings.GetterSuffixes;
import com.github.muehmar.gradle.openapi.generator.settings.GetterSuffixesBuilder;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.generator.settings.TestPojoSettings;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.writer.Writer;
import org.junit.jupiter.api.Test;

class RequiredNotNullableGetterTest {

  @Test
  void generator_when_requiredAndNotNullableField_then_correctOutputAndRefs() {
    final Generator<JavaPojoMember, PojoSettings> generator = RequiredNotNullableGetter.getter();
    final JavaPojoMember pojoMember =
        JavaPojoMembers.birthdate(Necessity.REQUIRED, Nullability.NOT_NULLABLE);

    final Writer writer =
        generator.generate(pojoMember, TestPojoSettings.defaultSettings(), Writer.createDefault());

    assertTrue(writer.getRefs().exists(JavaRefs.JAVA_TIME_LOCAL_DATE::equals));
    assertTrue(writer.getRefs().exists(Jakarta2ValidationRefs.NOT_NULL::equals));
    assertEquals(
        "/**\n"
            + " * Birthdate\n"
            + " */\n"
            + "@NotNull\n"
            + "public LocalDate getBirthdate() {\n"
            + "  return birthdate;\n"
            + "}",
        writer.asString());
  }

  @Test
  void generator_when_validationDisabled_then_correctOutputAndRefs() {
    final Generator<JavaPojoMember, PojoSettings> generator = RequiredNotNullableGetter.getter();
    final JavaPojoMember pojoMember =
        JavaPojoMembers.birthdate(Necessity.REQUIRED, Nullability.NOT_NULLABLE);

    final Writer writer =
        generator.generate(
            pojoMember,
            TestPojoSettings.defaultSettings().withEnableValidation(false),
            Writer.createDefault());

    assertTrue(writer.getRefs().exists(JavaRefs.JAVA_TIME_LOCAL_DATE::equals));
    assertEquals(
        "/**\n"
            + " * Birthdate\n"
            + " */\n"
            + "public LocalDate getBirthdate() {\n"
            + "  return birthdate;\n"
            + "}",
        writer.asString());
  }

  @Test
  void generator_when_requiredSuffix_then_correctOutputAndRefs() {
    final Generator<JavaPojoMember, PojoSettings> generator = RequiredNotNullableGetter.getter();
    final JavaPojoMember pojoMember =
        JavaPojoMembers.birthdate(Necessity.REQUIRED, Nullability.NOT_NULLABLE);

    final GetterSuffixes getterSuffixes =
        GetterSuffixesBuilder.create()
            .requiredSuffix("Req")
            .requiredNullableSuffix("")
            .optionalSuffix("")
            .optionalNullableSuffix("")
            .build();

    final Writer writer =
        generator.generate(
            pojoMember,
            TestPojoSettings.defaultSettings()
                .withEnableValidation(false)
                .withGetterSuffixes(getterSuffixes),
            Writer.createDefault());

    assertTrue(writer.getRefs().exists(JavaRefs.JAVA_TIME_LOCAL_DATE::equals));
    assertEquals(
        "/**\n"
            + " * Birthdate\n"
            + " */\n"
            + "public LocalDate getBirthdateReq() {\n"
            + "  return birthdate;\n"
            + "}",
        writer.asString());
  }

  @Test
  void generator_when_valueTypeOfArrayHasConstraints_then_correctOutputAndRefs() {
    final Generator<JavaPojoMember, PojoSettings> generator = RequiredNotNullableGetter.getter();

    final IntegerType itemType =
        IntegerType.formatInteger().withConstraints(Constraints.ofMin(new Min(5)));

    final JavaPojoMember member =
        JavaPojoMembers.list(
            itemType, Constraints.empty(), Necessity.REQUIRED, Nullability.NOT_NULLABLE);

    final Writer writer =
        generator.generate(
            member,
            TestPojoSettings.defaultSettings().withEnableValidation(true),
            Writer.createDefault());

    assertTrue(writer.getRefs().exists(Jakarta2ValidationRefs.NOT_NULL::equals));
    assertTrue(writer.getRefs().exists(Jakarta2ValidationRefs.MIN::equals));
    assertEquals(
        "/**\n"
            + " * List\n"
            + " */\n"
            + "@NotNull\n"
            + "public List<@Min(value = 5) Integer> getListVal() {\n"
            + "  return listVal;\n"
            + "}",
        writer.asString());
  }
}
