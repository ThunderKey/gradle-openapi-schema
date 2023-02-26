package com.github.muehmar.gradle.openapi.generator.java.generator.composedpojo;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaComposedPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaPojos;
import com.github.muehmar.gradle.openapi.generator.model.Discriminator;
import com.github.muehmar.gradle.openapi.generator.model.pojo.ComposedPojo;
import com.github.muehmar.gradle.openapi.generator.settings.TestPojoSettings;
import io.github.muehmar.codegenerator.writer.Writer;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(SnapshotExtension.class)
class ComposedPojoGeneratorTest {

  private Expect expect;

  public static Stream<Arguments> composedPojoVariants() {
    return PList.of(ComposedPojo.CompositionType.values())
        .flatMap(
            type ->
                PList.of(
                    JavaPojos.composedPojo(type),
                    JavaPojos.composedPojoWithDiscriminator(type),
                    JavaPojos.composedPojoWithDiscriminatorMapping(type)))
        .map(pojo -> Arguments.arguments(pojo.getCompositionType(), pojo.getDiscriminator(), pojo))
        .toStream();
  }

  @ParameterizedTest
  @SnapshotName("ComposedPojoGenerator")
  @MethodSource("composedPojoVariants")
  void generate_when_composedPojo_then_matchSnapshot(
      ComposedPojo.CompositionType type,
      Optional<Discriminator> discriminator,
      JavaComposedPojo pojo) {
    final ComposedPojoGenerator generator = new ComposedPojoGenerator();

    final Writer writer =
        generator.generate(pojo, TestPojoSettings.defaultSettings(), Writer.createDefault());

    final String scenario =
        PList.of(type, discriminator.orElse(null))
            .mkString(",")
            .replace("=", "->")
            .replace("[", "(")
            .replace("]", ")");

    expect.scenario(scenario).toMatchSnapshot(writer.asString());
  }
}
