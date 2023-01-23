package com.github.muehmar.gradle.openapi.generator.settings;

import ch.bluecare.commons.data.PList;
import java.util.Collections;

public class TestPojoSettings {
  private TestPojoSettings() {}

  public static PojoSettings defaultSettings() {
    return PojoSettingsBuilder.create()
        .jsonSupport(JsonSupport.JACKSON)
        .packageName("com.github.muehmar")
        .suffix("Dto")
        .enableSafeBuilder(true)
        .builderMethodPrefix("set")
        .enableValidation(true)
        .validationApi(ValidationApi.JAKARTA_2_0)
        .classTypeMappings(Collections.emptyList())
        .formatTypeMappings(Collections.emptyList())
        .enumDescriptionSettings(EnumDescriptionSettings.disabled())
        .getterSuffixes(defaultGetterSuffixes())
        .rawGetter(defaultRawGetter())
        .excludeSchemas(Collections.emptyList())
        .andAllOptionals()
        .build();
  }

  public static PList<PojoSettings> validationVariants() {
    return PList.of(
        defaultSettings().withEnableValidation(false),
        defaultSettings().withEnableValidation(true).withValidationApi(ValidationApi.JAKARTA_2_0),
        defaultSettings().withEnableValidation(true).withValidationApi(ValidationApi.JAKARTA_3_0));
  }

  public static GetterSuffixes defaultGetterSuffixes() {
    return GetterSuffixesBuilder.create()
        .requiredSuffix("")
        .requiredNullableSuffix("")
        .optionalSuffix("")
        .optionalNullableSuffix("")
        .build();
  }

  public static RawGetter defaultRawGetter() {
    return RawGetterBuilder.create()
        .modifier(JavaModifier.PRIVATE)
        .suffix("Raw")
        .deprecatedAnnotation(false)
        .andAllOptionals()
        .build();
  }
}
