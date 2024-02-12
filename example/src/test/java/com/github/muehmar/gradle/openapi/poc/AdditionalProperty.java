package com.github.muehmar.gradle.openapi.poc;

import java.util.Objects;

public class AdditionalProperty<T> {
  private final String name;
  private final T value;

  public AdditionalProperty(String name, T value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final AdditionalProperty<?> that = (AdditionalProperty<?>) o;
    return Objects.equals(name, that.name) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "AdditionalProperty{" + "name='" + name + '\'' + ", value=" + value + '}';
  }
}
