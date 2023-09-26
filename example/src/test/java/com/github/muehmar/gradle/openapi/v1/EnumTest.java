package com.github.muehmar.gradle.openapi.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muehmar.gradle.openapi.util.MapperFactory;
import openapischema.example.api.v1.model.InlineEnumDto;
import openapischema.example.api.v1.model.RootEnumDto;
import org.junit.jupiter.api.Test;

class EnumTest {
  private static final ObjectMapper MAPPER = MapperFactory.mapper();

  @Test
  void getDescription_when_calledForRootEnum_then_correspondingDescriptionReturned() {
    assertEquals("User role", RootEnumDto.USER.getDescription());
    assertEquals("Administrator role", RootEnumDto.ADMIN.getDescription());
    assertEquals("Visitor role", RootEnumDto.VISITOR.getDescription());
  }

  @Test
  void writeValueAsString_when_rootEnum_then_correctJson() throws JsonProcessingException {
    final RootEnumDto dto = RootEnumDto.USER;
    assertEquals("\"User\"", MAPPER.writeValueAsString(dto));
  }

  @Test
  void writeValueAsString_when_inlineEnum_then_correctJson() throws JsonProcessingException {
    final InlineEnumDto dto =
        InlineEnumDto.builder().andAllOptionals().setRole(InlineEnumDto.RoleEnum.ADMIN).build();
    assertEquals("{\"role\":\"Admin\"}", MAPPER.writeValueAsString(dto));
  }

  @Test
  void getDescription_when_calledForInlineEnum_then_correspondingDescriptionReturned() {
    assertEquals("User role", InlineEnumDto.RoleEnum.USER.getDescription());
    assertEquals("Administrator role", InlineEnumDto.RoleEnum.ADMIN.getDescription());
    assertEquals("Visitor role", InlineEnumDto.RoleEnum.VISITOR.getDescription());
  }

  @Test
  void readValue_when_rootEnum_then_correctJson() throws JsonProcessingException {
    assertEquals(RootEnumDto.USER, MAPPER.readValue("\"User\"", RootEnumDto.class));
  }

  @Test
  void readValue_when_inlineEnum_then_correctJson() throws JsonProcessingException {
    final InlineEnumDto dto =
        InlineEnumDto.builder().andAllOptionals().setRole(InlineEnumDto.RoleEnum.ADMIN).build();
    assertEquals(dto, MAPPER.readValue("{\"role\":\"Admin\"}", InlineEnumDto.class));
  }
}
