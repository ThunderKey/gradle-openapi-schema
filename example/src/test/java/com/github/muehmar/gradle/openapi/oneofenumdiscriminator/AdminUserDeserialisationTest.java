package com.github.muehmar.gradle.openapi.oneofenumdiscriminator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.muehmar.gradle.openapi.oneof.AdminOrUserDiscriminatorMappingDto;
import com.github.muehmar.gradle.openapi.util.MapperFactory;
import com.github.muehmar.openapi.util.Tristate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AdminUserDeserialisationTest {
  private static final ObjectMapper MAPPER = MapperFactory.mapper();

  @Test
  void fold_when_matchesAdmin_then_adminDtoReturned() throws JsonProcessingException {
    final AdminOrUserDto adminOrUserDto =
        MAPPER.readValue(
            "{\"id\":\"admin-id\",\"type\":\"admin\",\"adminname\":\"admin-name\",\"level\":5.5}",
            AdminOrUserDto.class);

    final Object obj = adminOrUserDto.foldOneOf(admin -> admin, user -> user);

    final AdminDto adminDto =
        AdminDto.builder()
            .setType(BaseUserDto.TypeEnum.ADMIN)
            .setId("admin-id")
            .setAdminname("admin-name")
            .andAllOptionals()
            .setLevel(5L)
            .build();
    assertEquals(adminDto, obj);
    assertEquals(Optional.empty(), adminOrUserDto.getUserDto());
    assertEquals(Optional.of(adminDto), adminOrUserDto.getAdminDto());
  }

  @Test
  void fold_when_matchesUser_then_userDtoReturned() throws JsonProcessingException {
    final AdminOrUserDto adminOrUserDto =
        MAPPER.readValue(
            "{\"id\":\"user-id\",\"type\":\"user\",\"username\":\"user-name\",\"age\":25,\"email\":null}",
            AdminOrUserDto.class);

    final Object obj = adminOrUserDto.foldOneOf(admin -> admin, user -> user);

    final UserDto userDto =
        UserDto.builder()
            .setType(BaseUserDto.TypeEnum.USER)
            .setId("user-id")
            .setUsername("user-name")
            .andAllOptionals()
            .setAge(25)
            .setEmail(Tristate.ofNull())
            .build();
    assertEquals(userDto, obj);
  }

  @Test
  void fold_when_invalidTypeWithoutOnInvalid_then_exceptionThrown() throws JsonProcessingException {
    final AdminOrUserDiscriminatorMappingDto adminOrUserDto =
        MAPPER.readValue(
            "{\"id\":\"admin-id\",\"type\":\"Admin\",\"adminname\":\"admin-name\",\"level\":5.5}",
            AdminOrUserDiscriminatorMappingDto.class);

    assertThrows(
        IllegalStateException.class, () -> adminOrUserDto.foldOneOf(admin -> admin, user -> user));
  }

  @Test
  void fold_when_invalidTypeWithOnInvalid_then_onInvalidReturned() throws JsonProcessingException {
    final AdminOrUserDiscriminatorMappingDto adminOrUserDto =
        MAPPER.readValue(
            "{\"id\":\"admin-id\",\"type\":\"Admin\",\"adminname\":\"admin-name\",\"level\":5.5}",
            AdminOrUserDiscriminatorMappingDto.class);

    final Object obj = adminOrUserDto.foldOneOf(admin -> admin, user -> user, () -> "invalid");

    assertEquals("invalid", obj);
  }
}
