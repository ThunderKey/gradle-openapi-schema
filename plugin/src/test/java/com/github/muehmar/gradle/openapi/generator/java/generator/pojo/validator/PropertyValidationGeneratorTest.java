package com.github.muehmar.gradle.openapi.generator.java.generator.pojo.validator;

import static com.github.muehmar.gradle.openapi.generator.java.generator.pojo.validator.PropertyValidationGenerator.propertyValidationGenerator;
import static com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers.list;
import static com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers.map;
import static com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers.requiredDouble;
import static com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers.requiredInteger;
import static com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMembers.requiredString;
import static com.github.muehmar.gradle.openapi.generator.settings.TestPojoSettings.defaultTestSettings;
import static io.github.muehmar.codegenerator.writer.Writer.javaWriter;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaPojoMember;
import com.github.muehmar.gradle.openapi.generator.java.model.type.JavaNumericType;
import com.github.muehmar.gradle.openapi.generator.java.model.type.JavaStringType;
import com.github.muehmar.gradle.openapi.generator.model.Necessity;
import com.github.muehmar.gradle.openapi.generator.model.Nullability;
import com.github.muehmar.gradle.openapi.generator.model.constraints.Constraints;
import com.github.muehmar.gradle.openapi.generator.model.constraints.DecimalMax;
import com.github.muehmar.gradle.openapi.generator.model.constraints.DecimalMin;
import com.github.muehmar.gradle.openapi.generator.model.constraints.Size;
import com.github.muehmar.gradle.openapi.generator.model.type.ArrayType;
import com.github.muehmar.gradle.openapi.generator.model.type.NumericType;
import com.github.muehmar.gradle.openapi.generator.model.type.StringType;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.generator.settings.TypeMappings;
import com.github.muehmar.gradle.openapi.snapshot.SnapshotTest;
import com.github.muehmar.gradle.openapi.snapshot.SnapshotUtil;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.writer.Writer;
import org.junit.jupiter.api.Test;

@SnapshotTest
class PropertyValidationGeneratorTest {
  private Expect expect;

  @Test
  @SnapshotName("integerWithMinAndMax")
  void generate_when_integerWithMinAndMax_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final Writer writer =
        generator.generate(requiredInteger(), defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("stringWithSize")
  void generate_when_stringWithMinAndMaxLength_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember stringType =
        requiredString()
            .withJavaType(
                JavaStringType.wrap(
                    StringType.noFormat().withConstraints(Constraints.ofSize(Size.of(10, 50))),
                    TypeMappings.empty()));

    final Writer writer = generator.generate(stringType, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("listWithSize")
  void generate_when_listWithMinAndMaxLength_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember listType =
        list(
            StringType.noFormat(),
            Necessity.REQUIRED,
            Nullability.NOT_NULLABLE,
            Constraints.ofSize(Size.of(10, 50)));

    final Writer writer = generator.generate(listType, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("mapWithSize")
  void generate_when_mapWithMinAndMaxLength_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember listType =
        map(
            StringType.noFormat(),
            StringType.noFormat(),
            Necessity.REQUIRED,
            Nullability.NOT_NULLABLE,
            Constraints.ofSize(Size.of(10, 50)));

    final Writer writer = generator.generate(listType, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("byteArraySize")
  void generate_when_byteArrayWithMinAndMaxLength_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember byteArrayType =
        requiredString()
            .withJavaType(
                JavaStringType.wrap(
                    StringType.ofFormat(StringType.Format.BINARY)
                        .withConstraints(Constraints.ofSize(Size.of(10, 50))),
                    TypeMappings.empty()));

    final Writer writer = generator.generate(byteArrayType, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("doubleWithDecimalMinMaxExclusive")
  void generate_when_doubleWithDecimalMinMaxExclusive_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember doubleMember =
        requiredDouble()
            .withJavaType(
                JavaNumericType.wrap(
                    NumericType.formatDouble()
                        .withConstraints(
                            Constraints.ofDecimalMinAndMax(
                                new DecimalMin("50.1", false), new DecimalMax("100.1", false))),
                    TypeMappings.empty()));

    final Writer writer = generator.generate(doubleMember, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("doubleWithDecimalMinMaxInclusive")
  void generate_when_doubleWithDecimalMinMaxInclusive_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final JavaPojoMember doubleMember =
        requiredDouble()
            .withJavaType(
                JavaNumericType.wrap(
                    NumericType.formatDouble()
                        .withConstraints(
                            Constraints.ofDecimalMinAndMax(
                                new DecimalMin("50.1", true), new DecimalMax("100.1", true))),
                    TypeMappings.empty()));

    final Writer writer = generator.generate(doubleMember, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("stringWithPattern")
  void generate_when_stringWithPattern_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final Writer writer = generator.generate(requiredString(), defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }

  @Test
  @SnapshotName("mapWithListValueType")
  void generate_when_mapWithListValueType_then_matchSnapshot() {
    final Generator<JavaPojoMember, PojoSettings> generator = propertyValidationGenerator();

    final StringType stringType =
        StringType.noFormat().withConstraints(Constraints.ofSize(Size.ofMax(50)));
    final ArrayType listType =
        ArrayType.ofItemType(stringType).withConstraints(Constraints.ofSize(Size.ofMin(8)));
    final JavaPojoMember mapType =
        map(
            StringType.noFormat(),
            listType,
            Necessity.REQUIRED,
            Nullability.NOT_NULLABLE,
            Constraints.ofSize(Size.of(10, 50)));

    final Writer writer = generator.generate(mapType, defaultTestSettings(), javaWriter());

    expect.toMatchSnapshot(SnapshotUtil.writerSnapshot(writer));
  }
}
