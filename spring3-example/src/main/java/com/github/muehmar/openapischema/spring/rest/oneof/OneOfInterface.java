package com.github.muehmar.openapischema.spring.rest.oneof;

import openapischema.spring3example.api.v1.model.AdminOrUserDiscriminatorDto;

public interface OneOfInterface {
  void post(AdminOrUserDiscriminatorDto dto);

  AdminOrUserDiscriminatorDto get();
}
