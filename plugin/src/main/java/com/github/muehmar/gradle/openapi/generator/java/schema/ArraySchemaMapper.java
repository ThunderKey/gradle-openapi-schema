package com.github.muehmar.gradle.openapi.generator.java.schema;

import com.github.muehmar.gradle.openapi.generator.constraints.Constraints;
import com.github.muehmar.gradle.openapi.generator.data.MappedSchema;
import com.github.muehmar.gradle.openapi.generator.data.Name;
import com.github.muehmar.gradle.openapi.generator.data.OpenApiPojo;
import com.github.muehmar.gradle.openapi.generator.java.JavaResolver;
import com.github.muehmar.gradle.openapi.generator.java.type.JavaType;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class ArraySchemaMapper extends BaseSchemaMapper<ArraySchema> {

  public ArraySchemaMapper(JavaSchemaMapper nextTypeMapper) {
    super(ArraySchema.class, nextTypeMapper);
  }

  @Override
  MappedSchema<JavaType> mapSpecificSchema(
      Name pojoName,
      Name pojoMemberName,
      ArraySchema schema,
      PojoSettings pojoSettings,
      JavaSchemaMapper chain) {
    final Schema<?> items = schema.getItems();

    final Constraints constraints = ConstraintsMapper.getMinAndMaxItems(schema);

    if (items instanceof ObjectSchema) {
      final Name openApiPojoName = JavaResolver.toPascalCase(pojoName, pojoMemberName);
      final JavaType itemType = JavaType.ofOpenApiSchema(openApiPojoName, pojoSettings.getSuffix());
      final JavaType javaList = JavaType.javaList(itemType);
      final OpenApiPojo openApiPojo = new OpenApiPojo(openApiPojoName, items);
      return MappedSchema.ofTypeAndOpenApiPojo(javaList.withConstraints(constraints), openApiPojo);
    } else {
      return chain
          .mapSchema(pojoName, pojoMemberName, items, pojoSettings, chain)
          .mapType(itemType -> JavaType.javaList(itemType).withConstraints(constraints));
    }
  }
}
