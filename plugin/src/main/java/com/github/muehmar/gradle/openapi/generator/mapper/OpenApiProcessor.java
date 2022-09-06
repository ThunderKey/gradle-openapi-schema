package com.github.muehmar.gradle.openapi.generator.mapper;

import com.github.muehmar.gradle.openapi.generator.model.OpenApiPojo;
import com.github.muehmar.gradle.openapi.generator.settings.PojoSettings;
import com.github.muehmar.gradle.openapi.util.Optionals;
import java.util.Optional;

interface OpenApiProcessor {

  /**
   * Processes an {@link OpenApiPojo}. If this processor is not capable of processing the
   * corresponding schema an empty {@link Optional} will get returned.
   */
  Optional<SchemaProcessResult> process(
      OpenApiPojo openApiPojo,
      PojoSettings pojoSettings,
      CompleteOpenApiProcessor completeOpenApiProcessor);

  default OpenApiProcessor or(OpenApiProcessor next) {
    final OpenApiProcessor self = this;
    return (openApiPojo, pojoSettings, completeOpenApiProcessor) ->
        Optionals.or(
            self.process(openApiPojo, pojoSettings, completeOpenApiProcessor),
            next.process(openApiPojo, pojoSettings, completeOpenApiProcessor));
  }

  default CompleteOpenApiProcessor orLast(OpenApiProcessor next) {
    final OpenApiProcessor openApiProcessor = or(next);

    return CompleteOpenApiProcessorImpl.ofChained(openApiProcessor);
  }
}
