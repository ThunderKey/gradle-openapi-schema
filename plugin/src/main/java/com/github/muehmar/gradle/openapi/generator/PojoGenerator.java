package com.github.muehmar.gradle.openapi.generator;

import com.github.muehmar.gradle.openapi.generator.model.Pojo;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.writer.GeneratedFile;

@FunctionalInterface
public interface PojoGenerator {
  GeneratedFile generatePojo(Pojo pojo, PojoSettings pojoSettings);
}
