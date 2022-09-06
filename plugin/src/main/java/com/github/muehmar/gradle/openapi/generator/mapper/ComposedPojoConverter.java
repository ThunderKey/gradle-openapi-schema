package com.github.muehmar.gradle.openapi.generator.mapper;

import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.model.ComposedPojo;
import com.github.muehmar.gradle.openapi.generator.model.OpenApiPojo;
import com.github.muehmar.gradle.openapi.generator.model.Pojo;
import com.github.muehmar.gradle.openapi.generator.model.PojoMember;
import com.github.muehmar.gradle.openapi.generator.model.PojoName;
import java.util.Optional;
import java.util.function.Function;

class ComposedPojoConverter {
  private ComposedPojoConverter() {}

  /**
   * Converts {@link ComposedPojo}'s to actual {@link Pojo}'s. The resulting list contains all
   * supplied pojo's as well as the converted ones.
   */
  public static PList<Pojo> convert(PList<ComposedPojo> composedPojos, PList<Pojo> pojos) {

    final PList<SchemaProcessResult> conversionResult =
        composedPojos
            .filter(
                composedPojo -> composedPojo.getType().equals(ComposedPojo.CompositionType.ALL_OF))
            .map(
                composedPojo -> {
                  final PList<PojoName> pojoNames = composedPojo.getPojoNames();
                  final PList<PojoName> openApiPojoNames =
                      composedPojo.getOpenApiPojos().map(OpenApiPojo::getPojoName);

                  final PList<Optional<Pojo>> foundPojos =
                      pojoNames
                          .concat(openApiPojoNames)
                          .map(
                              name ->
                                  pojos.find(
                                      pojo ->
                                          pojo.getPojoName()
                                              .asString()
                                              .equalsIgnoreCase(name.asString())));
                  if (foundPojos.exists(p -> !p.isPresent())) {
                    return SchemaProcessResult.ofComposedPojo(composedPojo);
                  } else {
                    final PList<PojoMember> members =
                        foundPojos.flatMapOptional(Function.identity()).flatMap(Pojo::getMembers);
                    final Pojo pojo =
                        Pojo.ofObject(
                            composedPojo.getName().getName(),
                            composedPojo.getDescription(),
                            composedPojo.getSuffix(),
                            members);
                    return SchemaProcessResult.ofPojo(pojo);
                  }
                });

    final PList<Pojo> newPojos = conversionResult.flatMap(SchemaProcessResult::getPojos);
    final PList<ComposedPojo> unconvertedComposedPojos =
        conversionResult.flatMap(SchemaProcessResult::getComposedPojos);
    if (newPojos.isEmpty() && unconvertedComposedPojos.nonEmpty()) {
      throw new IllegalStateException(
          "Unable to resolve schemas of composed schema: " + unconvertedComposedPojos);
    } else if (unconvertedComposedPojos.isEmpty()) {
      return pojos.concat(newPojos);
    } else {
      return convert(unconvertedComposedPojos, pojos.concat(newPojos));
    }
  }
}
