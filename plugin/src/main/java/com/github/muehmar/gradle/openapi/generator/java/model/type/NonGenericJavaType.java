package com.github.muehmar.gradle.openapi.generator.java.model.type;

import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.java.model.QualifiedClassName;
import com.github.muehmar.gradle.openapi.generator.model.Name;
import com.github.muehmar.gradle.openapi.generator.model.Type;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class NonGenericJavaType extends BaseJavaType {
  protected NonGenericJavaType(QualifiedClassName className, Type type) {
    super(className, type);
  }

  @Override
  public PList<Name> getAllQualifiedClassNames() {
    return PList.single(qualifiedClassName.asName());
  }

  @Override
  public Name getFullClassName() {
    return qualifiedClassName.getClassName();
  }

  @Override
  public AnnotatedClassName getFullAnnotatedClassName(AnnotationsCreator creator) {
    return AnnotatedClassName.fromClassName(getFullClassName());
  }
}
