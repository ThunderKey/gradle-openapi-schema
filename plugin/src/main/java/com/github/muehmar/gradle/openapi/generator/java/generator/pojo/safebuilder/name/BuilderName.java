package com.github.muehmar.gradle.openapi.generator.java.generator.pojo.safebuilder.name;

import com.github.muehmar.gradle.openapi.generator.java.generator.pojo.safebuilder.allof.AllOfBuilderName;
import com.github.muehmar.gradle.openapi.generator.java.model.pojo.JavaObjectPojo;

public interface BuilderName {
  String currentName();

  static BuilderName initial(JavaObjectPojo pojo) {
    return AllOfBuilderName.initial(pojo);
  }
}
