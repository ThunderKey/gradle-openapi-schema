package com.github.muehmar.gradle.openapi.generator.java;

import ch.bluecare.commons.data.NonEmptyList;
import com.github.muehmar.gradle.openapi.generator.PojoGenerator;
import com.github.muehmar.gradle.openapi.generator.java.generator.array.ArrayPojoGenerator;
import com.github.muehmar.gradle.openapi.generator.java.generator.composedpojo.ComposedPojoGenerator;
import com.github.muehmar.gradle.openapi.generator.java.generator.enumpojo.EnumGenerator;
import com.github.muehmar.gradle.openapi.generator.java.generator.map.MapPojoGenerator;
import com.github.muehmar.gradle.openapi.generator.java.generator.pojo.ObjectPojoGenerator;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaFileName;
import com.github.muehmar.gradle.openapi.generator.java.model.JavaPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaArrayPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaComposedPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaEnumPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaMapPojo;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaObjectPojo;
import com.github.muehmar.gradle.openapi.generator.model.Pojo;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.writer.GeneratedFile;
import io.github.muehmar.codegenerator.writer.Writer;

public class JavaPojoGenerator implements PojoGenerator {

  @Override
  public NonEmptyList<GeneratedFile> generatePojo(Pojo pojo, PojoSettings pojoSettings) {
    final NonEmptyList<? extends JavaPojo> javaPojos =
        JavaPojo.wrap(pojo, pojoSettings.getTypeMappings());
    return javaPojos.map(javaPojo -> generatePojo(javaPojo, pojoSettings));
  }

  public GeneratedFile generatePojo(JavaPojo pojo, PojoSettings pojoSettings) {
    final Writer writer = Writer.createDefault();

    final String content =
        pojo.fold(
                arrayPojo -> generateArrayPojo(arrayPojo, writer, pojoSettings),
                enumPojo -> generateEnumPojo(enumPojo, writer, pojoSettings),
                objectPojo -> generateObjectPojo(objectPojo, writer, pojoSettings),
                composedPojo -> generateComposedPojo(composedPojo, writer, pojoSettings),
                mapPojo -> generateMapPojo(mapPojo, writer, pojoSettings))
            .asString();

    final JavaFileName javaFileName = JavaFileName.fromSettingsAndPojo(pojoSettings, pojo);
    return new GeneratedFile(javaFileName.asPath(), content);
  }

  private Writer generateMapPojo(JavaMapPojo mapPojo, Writer writer, PojoSettings pojoSettings) {
    final MapPojoGenerator mapPojoGenerator = new MapPojoGenerator();
    return mapPojoGenerator.generate(mapPojo, pojoSettings, writer);
  }

  private Writer generateComposedPojo(
      JavaComposedPojo composedPojo, Writer writer, PojoSettings pojoSettings) {
    final ComposedPojoGenerator generator = new ComposedPojoGenerator();
    return generator.generate(composedPojo, pojoSettings, writer);
  }

  private Writer generateObjectPojo(JavaObjectPojo pojo, Writer writer, PojoSettings pojoSettings) {
    final ObjectPojoGenerator objectPojoGenerator = new ObjectPojoGenerator();
    return objectPojoGenerator.generate(pojo, pojoSettings, writer);
  }

  private Writer generateEnumPojo(JavaEnumPojo pojo, Writer writer, PojoSettings pojoSettings) {
    final EnumGenerator generator = EnumGenerator.topLevel();
    return generator.generate(pojo.asEnumContent(), pojoSettings, writer);
  }

  private Writer generateArrayPojo(JavaArrayPojo pojo, Writer writer, PojoSettings pojoSettings) {
    final ArrayPojoGenerator arrayPojoGenerator = new ArrayPojoGenerator();
    return arrayPojoGenerator.generate(pojo, pojoSettings, writer);
  }
}
