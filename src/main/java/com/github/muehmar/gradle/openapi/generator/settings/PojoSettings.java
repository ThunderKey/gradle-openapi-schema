package com.github.muehmar.gradle.openapi.generator.settings;

import com.github.muehmar.gradle.openapi.OpenApiSchemaGeneratorExtension;
import java.util.Objects;
import org.gradle.api.Project;

public class PojoSettings {
  private final JsonSupport jsonSupport;
  private final String packageName;
  private final String suffix;

  private PojoSettings(JsonSupport jsonSupport, String packageName, String suffix) {
    this.jsonSupport = jsonSupport;
    this.packageName = packageName;
    this.suffix = suffix;
  }

  public static PojoSettings fromOpenApiSchemaGeneratorExtension(
      OpenApiSchemaGeneratorExtension extension, Project project) {
    return new PojoSettings(
        getJsonSupport(extension), extension.getPackageName(project), extension.getSuffix());
  }

  public String getPackageName() {
    return packageName;
  }

  public String getSuffix() {
    return suffix;
  }

  public JsonSupport getJsonSupport() {
    return jsonSupport;
  }

  private static JsonSupport getJsonSupport(OpenApiSchemaGeneratorExtension extension) {
    return extension
        .getJsonSupport()
        .map(
            jsonSupport -> {
              if ("jackson".equalsIgnoreCase(jsonSupport)) {
                return JsonSupport.JACKSON;
              } else {
                return JsonSupport.NONE;
              }
            })
        .orElse(JsonSupport.NONE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PojoSettings that = (PojoSettings) o;
    return jsonSupport == that.jsonSupport
        && Objects.equals(packageName, that.packageName)
        && Objects.equals(suffix, that.suffix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jsonSupport, packageName, suffix);
  }

  @Override
  public String toString() {
    return "PojoSettings{"
        + "jsonSupport="
        + jsonSupport
        + ", packageName='"
        + packageName
        + '\''
        + ", suffix='"
        + suffix
        + '\''
        + '}';
  }
}
