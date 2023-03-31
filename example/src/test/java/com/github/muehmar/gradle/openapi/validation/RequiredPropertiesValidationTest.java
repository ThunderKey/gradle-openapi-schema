package com.github.muehmar.gradle.openapi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import OpenApiSchema.example.api.validation.model.RequiredPropertiesDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RequiredPropertiesValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @ParameterizedTest
  @ValueSource(strings = {"{\"val1\":\"\",\"val2\":\"\"}", "{\"val1\":\"\",\"val2\":null}"})
  void validate_when_propertiesPresent_then_noViolations(String json)
      throws JsonProcessingException {
    final RequiredPropertiesDto dto = MAPPER.readValue(json, RequiredPropertiesDto.class);

    final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    final Validator validator = validatorFactory.getValidator();

    final Set<ConstraintViolation<RequiredPropertiesDto>> constraintViolations =
        validator.validate(dto);

    assertEquals(0, constraintViolations.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"val1\":\"\"}", "{\"val2\":\"\"}", "{\"val1\":null,\"val2\":\"\"}"})
  void validate_when_propertiesNotPresent_then_violations(String json)
      throws JsonProcessingException {
    final RequiredPropertiesDto dto = MAPPER.readValue(json, RequiredPropertiesDto.class);

    final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    final Validator validator = validatorFactory.getValidator();

    final Set<ConstraintViolation<RequiredPropertiesDto>> constraintViolations =
        validator.validate(dto);

    assertEquals(1, constraintViolations.size());
  }
}
