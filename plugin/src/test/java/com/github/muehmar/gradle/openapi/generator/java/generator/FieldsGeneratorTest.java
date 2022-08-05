package com.github.muehmar.gradle.openapi.generator.java.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.data.Pojo;
import com.github.muehmar.gradle.openapi.generator.java.JacksonRefs;
import com.github.muehmar.gradle.openapi.generator.java.generator.data.Pojos;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.generator.settings.TestPojoSettings;
import io.github.muehmar.codegenerator.Generator;
import io.github.muehmar.codegenerator.writer.Writer;
import org.junit.jupiter.api.Test;

class FieldsGeneratorTest {

  @Test
  void fields_when_samplePojo_then_correctOutputAndRef() {
    final Generator<Pojo, PojoSettings> gen = FieldsGenerator.fields();

    final Writer writer =
        gen.generate(
            Pojos.allNecessityAndNullabilityVariants(),
            TestPojoSettings.defaultSettings(),
            Writer.createDefault());

    assertEquals(PList.empty(), writer.getRefs());

    final String output = writer.asString();
    assertEquals(
        "private final String requiredStringVal;\n"
            + "private final String requiredNullableStringVal;\n"
            + "private final boolean isRequiredNullableStringValPresent;\n"
            + "private final String optionalStringVal;\n"
            + "private final String optionalNullableStringVal;\n"
            + "private final boolean isOptionalNullableStringValNull;",
        output);
  }

  @Test
  void fields_when_arrayPojo_then_correctOutputAndRef() {
    final Generator<Pojo, PojoSettings> gen = FieldsGenerator.fields();

    final Writer writer =
        gen.generate(Pojos.array(), TestPojoSettings.defaultSettings(), Writer.createDefault());

    assertTrue(writer.getRefs().exists(JacksonRefs.JSON_VALUE::equals));

    final String output = writer.asString();
    assertEquals("@JsonValue\n" + "private final List<String> value;", output);
  }
}
