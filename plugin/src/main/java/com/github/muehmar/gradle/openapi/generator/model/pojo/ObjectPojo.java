package com.github.muehmar.gradle.openapi.generator.model.pojo;

import static com.github.muehmar.gradle.openapi.generator.model.pojo.ObjectPojoBuilder.fullObjectPojoBuilder;
import static com.github.muehmar.gradle.openapi.util.Booleans.not;

import ch.bluecare.commons.data.NonEmptyList;
import ch.bluecare.commons.data.PList;
import com.github.muehmar.gradle.openapi.generator.model.AdditionalProperties;
import com.github.muehmar.gradle.openapi.generator.model.Pojo;
import com.github.muehmar.gradle.openapi.generator.model.PojoMember;
import com.github.muehmar.gradle.openapi.generator.model.Type;
import com.github.muehmar.gradle.openapi.generator.model.composition.AllOfComposition;
import com.github.muehmar.gradle.openapi.generator.model.composition.AnyOfComposition;
import com.github.muehmar.gradle.openapi.generator.model.composition.OneOfComposition;
import com.github.muehmar.gradle.openapi.generator.model.constraints.Constraints;
import com.github.muehmar.gradle.openapi.generator.model.name.ComponentName;
import com.github.muehmar.gradle.openapi.generator.model.name.Name;
import com.github.muehmar.gradle.openapi.generator.model.name.PojoName;
import com.github.muehmar.gradle.openapi.generator.settings.PojoNameMapping;
import io.github.muehmar.pojobuilder.annotations.PojoBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@PojoBuilder
public class ObjectPojo implements Pojo {
  private final ComponentName name;
  private final String description;
  private final PList<PojoMember> members;
  private final PList<Name> requiredAdditionalProperties;
  private final Optional<AllOfComposition> allOfComposition;
  private final Optional<OneOfComposition> oneOfComposition;
  private final Optional<AnyOfComposition> anyOfComposition;
  private final Constraints constraints;
  private final AdditionalProperties additionalProperties;

  ObjectPojo(
      ComponentName name,
      String description,
      PList<PojoMember> members,
      PList<Name> requiredAdditionalProperties,
      Optional<AllOfComposition> allOfComposition,
      Optional<OneOfComposition> oneOfComposition,
      Optional<AnyOfComposition> anyOfComposition,
      Constraints constraints,
      AdditionalProperties additionalProperties) {
    this.name = name;
    this.description = description;
    this.members = members;
    this.requiredAdditionalProperties = requiredAdditionalProperties;
    this.allOfComposition = allOfComposition;
    this.oneOfComposition = oneOfComposition;
    this.anyOfComposition = anyOfComposition;
    this.constraints = constraints;
    this.additionalProperties = additionalProperties;
  }

  @Override
  public ComponentName getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public PList<PojoMember> getMembers() {
    return members;
  }

  public Constraints getConstraints() {
    return constraints;
  }

  public PList<Name> getRequiredAdditionalProperties() {
    return requiredAdditionalProperties;
  }

  public AdditionalProperties getAdditionalProperties() {
    return additionalProperties;
  }

  public Optional<AllOfComposition> getAllOfComposition() {
    return allOfComposition;
  }

  public Optional<OneOfComposition> getOneOfComposition() {
    return oneOfComposition;
  }

  public Optional<AnyOfComposition> getAnyOfComposition() {
    return anyOfComposition;
  }

  public boolean allowsAdditionalProperties() {
    return additionalProperties.isAllowed();
  }

  @Override
  public Pojo addObjectTypeDescription(PojoName objectTypeName, String description) {
    return mapMembers(member -> member.addObjectTypeDescription(objectTypeName, description));
  }

  @Override
  public ObjectPojo inlineObjectReference(
      PojoName referenceName, String referenceDescription, Type referenceType) {
    final PList<PojoMember> mappedMembers =
        members.map(
            member ->
                member.inlineObjectReference(referenceName, referenceDescription, referenceType));
    final Optional<AllOfComposition> mappedAllOfComposition =
        allOfComposition.map(
            composition ->
                composition.inlineObjectReference(
                    referenceName, referenceDescription, referenceType));
    final Optional<OneOfComposition> mappedOneOfComposition =
        oneOfComposition.map(
            composition ->
                composition.inlineObjectReference(
                    referenceName, referenceDescription, referenceType));
    final Optional<AnyOfComposition> mappedAnyOfComposition =
        anyOfComposition.map(
            composition ->
                composition.inlineObjectReference(
                    referenceName, referenceDescription, referenceType));
    final AdditionalProperties mappedAdditionalProperties =
        additionalProperties.inlineObjectReference(referenceName, referenceType);
    return fullObjectPojoBuilder()
        .name(name)
        .description(description)
        .members(mappedMembers)
        .requiredAdditionalProperties(requiredAdditionalProperties)
        .constraints(constraints)
        .additionalProperties(mappedAdditionalProperties)
        .allOfComposition(mappedAllOfComposition)
        .oneOfComposition(mappedOneOfComposition)
        .anyOfComposition(mappedAnyOfComposition)
        .build();
  }

  @Override
  public ObjectPojo applyMapping(PojoNameMapping pojoNameMapping) {
    final PList<PojoMember> mappedMembers =
        members.map(member -> member.applyMapping(pojoNameMapping));
    final Optional<AllOfComposition> mappedAllOfComposition =
        allOfComposition.map(composition -> composition.applyMapping(pojoNameMapping));
    final Optional<OneOfComposition> mappedOneOfComposition =
        oneOfComposition.map(composition -> composition.applyMapping(pojoNameMapping));
    final Optional<AnyOfComposition> mappedAnyOfComposition =
        anyOfComposition.map(composition -> composition.applyMapping(pojoNameMapping));
    final AdditionalProperties mappedAdditionalProperties =
        additionalProperties.applyMapping(pojoNameMapping);
    return fullObjectPojoBuilder()
        .name(name.applyPojoMapping(pojoNameMapping))
        .description(description)
        .members(mappedMembers)
        .requiredAdditionalProperties(requiredAdditionalProperties)
        .constraints(constraints)
        .additionalProperties(mappedAdditionalProperties)
        .allOfComposition(mappedAllOfComposition)
        .oneOfComposition(mappedOneOfComposition)
        .anyOfComposition(mappedAnyOfComposition)
        .build();
  }

  private ObjectPojo mapMembers(UnaryOperator<PojoMember> map) {
    return new ObjectPojo(
        name,
        description,
        members.map(map),
        requiredAdditionalProperties,
        allOfComposition,
        oneOfComposition,
        anyOfComposition,
        constraints,
        additionalProperties);
  }

  @Override
  public <T> T fold(
      Function<ObjectPojo, T> onObjectPojo,
      Function<ArrayPojo, T> onArrayType,
      Function<EnumPojo, T> onEnumPojo) {
    return onObjectPojo.apply(this);
  }

  public boolean containsNoneDefaultPropertyScope() {
    return members.exists(member -> not(member.isDefaultScope()))
        || containsNonDefaultPropertyScope(allOfComposition.map(AllOfComposition::getPojos))
        || containsNonDefaultPropertyScope(oneOfComposition.map(OneOfComposition::getPojos))
        || containsNonDefaultPropertyScope(anyOfComposition.map(AnyOfComposition::getPojos));
  }

  private static boolean containsNonDefaultPropertyScope(Optional<NonEmptyList<Pojo>> pojos) {
    return pojos
        .map(NonEmptyList::toPList)
        .orElseGet(PList::empty)
        .flatMapOptional(Pojo::asObjectPojo)
        .exists(ObjectPojo::containsNoneDefaultPropertyScope);
  }
}
