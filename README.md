[![Build Status](https://github.com/muehmar/gradle-openapi-schema/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/muehmar/gradle-openapi-schema/actions/workflows/gradle.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/muehmar/gradle-openapi-schema/blob/master/LICENSE)

# Gradle OpenApi Schema Codegen

This is a gradle plugin to generate Java code given an openapi 3.0.x or 3.1.0 specification. Unlike other codegen tools, this
focuses mainly on the `#/component/schema` section. It generates immutable classes and special builder classes to
support a safe way creating instances. The data classes support JSON conversions via jackson. Additionally, the plugin
generates simple classes for parameters (`#/component/parameters` section) to support checking the constraints.

* Immutable Java classes
* Special builder pattern for safe creation of instances
* JSON deserializing and serializing support with jackson
* Customization of the code generation
* Support for Java Bean Validation 2.x and Jakarta Bean Validation 2.x / 3.x
* Additional validation of object level constraints
* Extraction of description for enums
* Supports processing multiple specifications
* Simple classes for parameters
* Support compositions (`allOf`, `anyOf`, `oneOf`)

The implementation is based on the
[swagger-parser](https://github.com/swagger-api/swagger-parser)
project.

## Usage

Add the plugin section in your `build.gradle`:

```
plugins {
    id 'com.github.muehmar.openapischema' version '1.1.2'
}
```

## Configuration

Add an `openApiGenerator` block into your `build.gradle` file:

```
openaApiGenerator {
   schemas {
       apiV1 {
            inputSpec = "$projectDir/src/main/resources/openapi-v1.yml"
       }
   }
}
```

or a full example:

```
openApiGenerator {
    schemas {    
    
        // Custom name for this schema
        apiV1 {         
            sourceSet = 'main'
            inputSpec = "$projectDir/src/main/resources/openapi-v1.yml"
            outputDir = "$buildDir/generated/openapi"
            packageName = "${project.group}.${project.name}.api.v1.model"
            jsonSupport = "jackson"
            suffix = "Dto"
            enableValidation = true
            validationApi = "jakarta-3.0"
            builderMethodPrefix = "set"

            // This would overwrite any global configuration
            enumDescriptionExtraction {
                enabled = true
                prefixMatcher = "`__ENUM__`:"
                failOnIncompleteDescriptions = true
            }

            // Additional format type mapping
            formatTypeMapping {
                formatType = "username"
                classType = "com.package.UserName"
            }

            // Additional format type mapping
            formatTypeMapping {
                formatType = "password"
                classType = "com.package.Password"
            }

            // Additional class mapping
            classMapping {
                fromClass = "List"
                toClass = "java.util.ArrayList"
            }
            
            getterSuffixes {
                requiredSuffix = "Req"
                requiredNullabeSuffix = "Opt"
                optionalSuffix = "Opt"
                optionalNullableSuffix = "Tristate"                
            }
            
            validationMethods {
                getterSuffix = "Raw"
                modifier = "public"
                deprecatedAnnotation = true
            }
        }
        
        // Custom name for this schema
        apiV2 {         
            sourceSet = 'main'
            inputSpec = "$projectDir/src/main/resources/openapi-v2.yml"
            outputDir = "$buildDir/generated/openapi"
            packageName = "${project.group}.${project.name}.api.v2.model"
            jsonSupport = "jackson"
            suffix = "Dto"
            enableValidation = true
            
            // No specific config for enum description extraction
            // or mappings. Will inherit the global configuration
        }
    }
    
    // Global configuration for enum description extraction, 
    // used in case no specific configuration is present
    enumDescriptionExtraction {
        enabled = true
        prefixMatcher = "`__ENUM__`:"
        failOnIncompleteDescriptions = true
    }

    // Global format type mapping which gets applied to each schema
    formatTypeMapping {
        formatType = "username"
        classType = "com.package.UserName"
    }

    // Global format type mapping which gets applied to each schema
    formatTypeMapping {
        formatType = "password"
        classType = "com.package.Password"
    }

    // Global class mapping which gets applied to each schema
    classMapping {
        fromClass = "List"
        toClass = "java.util.ArrayList"
    }
    
    getterSuffixes {
        // global config goes here
    }
    
    validationMethods {
       // global config goes here
    }
}
```

Add in the `schemas` block for each specification a new block with custom name (`apiV1` and `apiV2` in the example
above) and configure the generation with the following attributes for each schema:

| Key                 | Data Type    | Default                                    | Description                                                                                                                                                                                                                                                                          |
|---------------------|--------------|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| sourceSet           | String       | main                                       | Source set to which the generated classes should be added.                                                                                                                                                                                                                           |
| inputSpec           | String       |                                            | The OpenApi 3.x specification location.                                                                                                                                                                                                                                              |
| outputDir           | String       | $buildDir/generated/openapi                | The location in which the generated sources should be stored.                                                                                                                                                                                                                        |
| resolveInputSpecs   | boolean      | true                                       | Input specifications are resolved for task input calculation for gradle. This requires parsing the specification to identify remote specifications. This can be disabled if needed, see [Incremental build and remote specifications](#incremental-build-and-remote-specifications). |
| packageName         | String       | ${project.group}.${project.name}.api.model | Name of the package for the generated classes.                                                                                                                                                                                                                                       |
| suffix              | String       |                                            | Suffix which gets appended to each generated class. The classes are unchanged if no suffix is provided.                                                                                                                                                                              |
| jsonSupport         | String       | jackson                                    | Used json support library. Possible values are `jackson` or `none`.                                                                                                                                                                                                                  |
| enableSafeBuilder   | Boolean      | true                                       | Enables creating the safe builder.                                                                                                                                                                                                                                                   |
| enableValidation    | Boolean      | false                                      | Enables the generation of annotations for bean validation. Select with `validationApi` the used packages.                                                                                                                                                                            |
| validationApi       | String       | jakarta-2                                  | Defines the used annotations (either from `javax.*` or `jakarta.*` package). Possible values are `jakarta-2` and `jakarta-3`. Use for Java Bean validation 2.0 or Jakarta Bean validation `jakarata-2` and for Jakarta Bean validation 3.0 `jakarta-3`.                              |
| builderMethodPrefix | String       |                                            | Prefix for the setter method-name of builders. The default empty string leads to setter method-names equally to the corresponding fieldname.                                                                                                                                         |
| excludedSchemas     | List[String] | []                                         | Excludes the given schemas from generation. This can be used in case unsupported features are used, e.g. URL-references or unsupported compositions.                                                                                                                                 |

The plugin creates for each schema a task named `generate{NAME}Model` where `{NAME}` is replaced by the used name for
the schema, in the example above a task `generateApiV1Model` and a task `generateApiV2Model` would get created. The
tasks are automatically registered as dependency of the corresponding java-compile task.

### Class Mappings

The plugin allows one to map specific classes to custom types. The following example would use the custom List
implementation `com.package.CustomList` for lists instead of `java.util.List`. The config-property `toClass` should be
the fully qualified classname to properly generate import-statements.

```
classMapping {
    fromClass = "List"
    toClass = "com.package.CustomList"
}

```

Repeat this block for each class mapping.

### Format Type Mappings

The plugin also allows using custom classes for specific properties in the OpenApi specification. The properties must be
of type `string` and the format is a custom name which can be referenced in the plugin configuration to use the custom
class. For example the spec

```
  properties:
    accountName:
      type: string
      format: username
```

and a formatTypeMapping block in the configuration

```
formatTypeMapping {
    formatType = "username"
    classType = "com.package.UserName"
}
```

will use the class `com.package.UserName` for the property `accountName`. The config-property `classType` should be
the fully qualified classname to properly generate import-statements.

Repeat this block for each format type mapping.

### Enum description extraction

Enables and configures the extraction of a description for enums from the openapi specification.
The `enumDescriptionExtraction` block is optional.

```
enumDescriptionExtraction {
    enabled = true
    prefixMatcher = "`__ENUM__`:"
    failOnIncompleteDescriptions = true
}
```

| Key                          | Data Type | Default | Description                                                                                                                    |
|------------------------------|-----------|:--------|:-------------------------------------------------------------------------------------------------------------------------------|
| enabled                      | Boolean   | false   | Enables the extraction of descriptions for enum from the openapi specification.                                                |
| prefixMatcher                | String    |         | The prefix which matches the start of the description for the enums.                                                           |
| failOnIncompleteDescriptions | Boolean   | false   | Either no description or a description for each members of an enum must be present if set, otherwise the generation will fail. |

### Getter suffixes

This generator differentiates between 4 different properties (see chapter [Nullability](#Nullability)):

* Required
* Required and nullable
* Optional
* Optional and nullable

It is possible to customize the suffixes of these getters:

```
getterSuffixes {
    requiredSuffix = ""
    requiredNullabeSuffix = "Opt"
    optionalSuffix = "Opt"
    optionalNullableSuffix = "Tristate"                
}
```

| Key                    | Data Type | Default  | Description                                                             |
|------------------------|-----------|:---------|:------------------------------------------------------------------------|
| requiredSuffix         | String    |          | Suffix added to the getter methods for required properties              |
| requiredNullableSuffix | String    | Opt      | Suffix added to the getter methods for required and nullable properties |
| optionalSuffix         | String    | Opt      | Suffix added to the getter methods for optional properties              |
| optionalNullableSuffix | String    | Tristate | Suffix added to the getter methods for optional and nullable properties |

### Validation Methods

This generator creates classes where `null` is not used, either not as return value or as argument. Nullable or optional
properties are wrapped for example with `java.util.Optional`. Frameworks for serialisation or validation require to 
operate with nullable objects. The current supported framework for serialisation (Jackson) is able to work with private
methods which are generated by the plugin. The reference implementation for bean validation (hibernate) is also able
to work with private methods, but other frameworks like Spring (although may using hibernate) require to have public
methods for validation. 

Therefore, the generator allows to customize the generation of validation methods. It allows to 
change the access modifier of validations methods. Additionally, a deprecated annotation can be added to each validation
method, to point out that these methods should not be used in the code manually by the programmer but automatically by
frameworks. For getters of properties used for validation, a suffix can be configured to avoid the clash with the 
standard methods which return wrapped objects instead of nullable objects.

The following is an example to configure the generator to generate public validation methods and marked as deprecated 
which can be used together with the validation in Spring.

```
validationMethods {
    modifier = "public"
    deprecatedAnnotation = true
}
```

| Key                  | Data Type | Default | Description                                                                                          |
|----------------------|-----------|:--------|:-----------------------------------------------------------------------------------------------------|
| getterSuffix         | String    | Raw     | Suffix which is added to properties of getters which are only used for validation                    |
| modifier             | String    | private | Modifier for validation methods. Can be one of `public`, `protected`, `package-private` or `private` |
| deprecatedAnnotation | boolean   | false   | Determines if the validation methods should be annotated with deprecated.                            |

See the Spring-Example ([build.gradle](spring-example/build.gradle)) which makes use of this configuration.

## OpenAPI v3.0.x vs v3.1.0
The version 3.1.0 of the OpenAPI specification is not backwards compatible with 3.0.x, i.e. has some breaking changes. 
The most obvious change is the specification of the type, in 3.0.x it is a single property, whereas in 3.1.0 the type
is an array. This plugin does currently not support multiple types with one exception: the `null` type.

The following in v3.0.x:
```
type: string
nullable: true
```
is equivalent to in v3.1.0:
```
type:
  - string
  - null
```

Any other combination of types is currently not supported.

## Compositions
The OpenAPI specification supports the composition of schemas via `oneOf`, `anyOf` and `allOf` keyword. This plugin supports 
all three keywords. 

Validation is supported for all three compositions.

### AllOf
With `allOf`, the plugin will generate a DTO with all properties of the specified schemas. Consider the following 
specification:

```
components:
  schemas:
    User:
      required:
        - username
      properties:
        username:
          type: string
    Admin:
      required:
        - adminname
      properties:
        adminname:
          type: string

    AdminAndUser:
      anyOf:
        - $ref: '#/components/schemas/Admin'
        - $ref: '#/components/schemas/User'
```

This will generate the three DTO's, `User`, `Admin` and `AdminAndUser`. The `AdminAndUser` will contain both
properties of the `User` and the `Admin`, i.e. the `username` property and `adminname` property.

### AnyOf and OneOf
The usage of `anyOf` and `oneOf` will generate special classes used to represent this composition.

```
components:
  schemas:
    User:
      required:
        - username
      properties:
        username:
          type: string
    Admin:
      required:
        - adminname
      properties:
        adminname:
          type: string

    AdminOrUser:
      oneOf:
        - $ref: '#/components/schemas/Admin'
        - $ref: '#/components/schemas/User'

    AdminAndOrUser:
      anyOf:
        - $ref: '#/components/schemas/Admin'
        - $ref: '#/components/schemas/User'
```
The plugin will generate the following DTO's:
* `UserDto`: Simple DTO for the User schema 
* `AdminDto`: Simple DTO for the Admin schema 
* `AdminOrUserDto`: DTO for the `oneOf` composition
* `AdminAndOrUserDto`: DTO for the `anyOf` composition 

#### Construction
The generated composition class will contain factory methods to create an instance. An instance can be created 
from a DTO of the composition, i.e. from the `UserDto` or the `AdminDto`.
```
  public static AdminOrUserDto fromAdmin(AdminDto adminDto);
  
  public static AdminOrUserDto fromUser(UserDto adminDto);
```

As a `anyOf` composition can contain multiple DTO's, there exist-wither methods to add
more DTO's after instantiation:

```
  public AdminAndOrUserDto withAdmin(AdminDto adminDto);
  
  public AdminAndOrUserDto withUser(UserDto adminDto);
```


#### Decomposing `oneOf`
Two fold method exists to decompose a `oneOf` DTO:
```
  public <T> T fold(Function<AdminDto, T> onAdminDto, Function<UserDto, T> onUserDto);
  
  public <T> T fold(Function<AdminDto, T> onAdminDto, Function<UserDto, T> onUserDto, Supplier<T> onInvalid);
```

Both method accepts mapping functions for each schema, in the example case one function for the `AdminDto` and one
function for the `UserDto`. The corresponding function gets executed and its result is returnred.
I.e. if the `AdminOrUserDto` is valid against the Admin schema, the function `onAdminDto` gets executed with the
`AdminDto` as argument and the result is returned. The same applies analogously if the `AdminOrUserDto` is valid against
the User schema.

The second method has a Java-Supplier as third argument. This supplier gets called in case the DTO is not valid against
exactly one schema. The first method which has no supplier will throw an Exception in this case, this method can be used in case the
DTO is either manually or automatically validated before the decomposition.

#### Decomposing `anyOf`
There is a single fold method can be used to decompose an `anyOf` DTO:
```
  public <T> List<T> fold(Function<AdminDto, T> onAdminDto, Function<UserDto, T> onUserDto);
```
This method is similar to the fold method of the `oneOf` composition only that it returns a list as multiple mapping 
functions can be called. In case the DTO is valid against no schema, it will simply return and empty list.

### Examples
#### AnyOf
* [OpenAPI spec](example/src/main/resources/openapi-anyof.yml)
* [Object creation and serialisation](example/src/test/java/com/github/muehmar/gradle/openapi/anyof/TestSerialisation.java)
* [Deserialisation and decomposition](example/src/test/java/com/github/muehmar/gradle/openapi/anyof/TestDeserialisation.java)
* [Validation](example/src/test/java/com/github/muehmar/gradle/openapi/anyof/TestValidation.java)

#### OneOf
* [OpenAPI spec](example/src/main/resources/openapi-oneof.yml)
* [Object creation and serialisation](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestSerialisation.java)
* [Deserialisation and decomposition](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestDeserialisation.java)
* [Validation](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestValidation.java)
* [Object creation and serialisation with discriminator](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestDiscriminatorSerialisation.java)
* [Deserialisation and decomposition with discriminator](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestDiscriminatorDeserialisation.java)

## Nullability

With version 3.0.x of the OpenAPI specification one can declare a property to be nullable:

```
type: string
nullable: true
```

This plugin supports all possible combination of required/optional and nullable properties for serialisation (and
deserialisation) and validation. Required properties which are nullable as well as optional properties which are not
nullable are wrapped into a `java.util.Optional`. Optional properties which are nullable are wrapped into a
special `Tristate` class to properly model all three states (value present, null or absent).

| Required/Optional | Nullability  | Getter return type |
|-------------------|--------------|:-------------------|
| Required          | Not Nullable | T                  |
| Required          | Nullable     | Optional\<T>       |
| Optional          | Not Nullable | Optional\<T>       |
| Optional          | Nullable     | Tristate\<T>       |

### Tristate class

The special `Tristate` class is used for optional properties which are nullable. The `Tristate` class offers a compiler
enforced chain of methods to handle all possible cases:

```
  String result = dto.getOptionalNullableProperty()
    .onValue(val -> "Value: " + val)
    .onNull(() -> "Property was null")
    .onAbsent(() -> "Property was absent");
```

The `onValue` method accepts a `Function` as argument, which gets the value as input. The `onNull` and `onAbsent`
methods accepts a `Supplier` which gets executed in case the property was null or absent.

## Safe Builder

The 'Safe Builder' is an extended builder pattern which enforces one to create valid instances, i.e. every required
property in a class will be set.

This is done by creating a single builder class for each required property, with a single method setting the
corresponding property and returning the next builder for the next property. The `build`
method will only be present after each required property is set.

For example, given the schema:

```
components:
  schemas:
   User:
      required:
        - name
        - city
      properties:
        name:
          type: string
        city: 
          type: string
        age:
          type: integer
```

will lead to a builder which can be used like the following:

```
  User.newBuilder()
    .setName("Dexter")
    .setCity("Miami")
    .andAllOptionals()
    .setAge(39)
    .build();
```

This does not seem to be very different from the normal builder pattern at a first glance but calling `newBuilder()`
will return a class which has only a single method `setName()`, i.e. the compiler enforces one to set the name. The
returned class after setting the name has again one single method `setCity()`. As the property `city` is the last
required property in this example the returned class for `setCity()` offers three methods:

* `build()` As all required properties are set at that time, building the instance is allowed here.
* `andOptionals()` Returns the normal builder allowing one to set certain optional properties before creating the
  instance. This method returns just the normal builder populated with all required properties.
* `andAllOptionals()` Enforces one to set all optional properties in the same way as it is done for the required
  properties. The `build()` method will only be available after all optional properties have been set. This method is
  used in the example above, i.e. the compiler enforces one to set the `age` property too.

Setting all required properties in a class could theoretically also be achieved with a constructor with all required
properties as arguments, but the pattern used here is safer in terms of refactoring, i.e. adding or removing properties,
changing the required properties or changing the order of the properties.

When using `andAllOptionals()` or `andOptinoals()` after all required properties are set, the builder provides
overloaded methods to add the optional properties. The property can be set directly or wrapped in an `Optional`. In the
example above, the builder provides methods with the following signature:

```
  public Builder setAge(Integer age);
  
  public Builder setAge(Optional<Integer> age);
```

Note that the prefix of the methods is customizable, see the `Configuration` section.

## Validation

The generation of annotations for validation can be enabled by setting `enableValidation` to `true`. It requires at 
least version 2.0 of the java/jakarta validation api as dependency. It supports object graph validation via the `@Valid` 
annotation.

| Validation API          | Supported versions |
|-------------------------|--------------------|
| Java Bean Validation    | 2.0                |
| Jakarta Bean Validation | 2.0 and 3.0        |

### Type specific constraints

The following type specific constraints are supported:

| Type / Format                       | Keyword                                                       | Annotation                             | Remark                                                                |
|-------------------------------------|---------------------------------------------------------------|----------------------------------------|-----------------------------------------------------------------------|
| number / double<br/>number / float  | minimum<br/>exclusiveMinimum<br/>maximum<br/>exclusiveMaximum | `@DecimaleMin`<br/>`@DecimalMax`       |                                                                       |
| integer / int32<br/>integer / int64 | minimum<br/>exclusiveMinimum<br/>maximum<br/>exclusiveMaximum | `@Min`<br/>`@Max`                      |                                                                       |
| string                              | minLength<br/>maxLength                                       | `@Size`                                |                                                                       |
| string                              | pattern                                                       | `@Pattern`                             |                                                                       |
| string / email                      | -                                                             | `@Email`                               |                                                                       |
| array                               | minItems<br/>maxItems                                         | `@Size`                                |                                                                       |
| integer / number                    | multipleOf                                                    | Special validation method is generated | Validation for number types might be unreliable due to numeric errors |


### Required properties
The validation of required properties is supported through the `@NotNull` annotation. Required properties marked
as `nullable: true` (in v3.0.x) or with the additional type `null` (in v3.1.0) are also supported for validation.

### Object level validation

The following keywords are supported:

* `minProperties` and `maxProperties` for object types
* `uniqueItems` for array types

The plugin generates a method which returns the number of present properties of an object which is annotated with the
constraints (if present).

### Composition
The validation of composed objects with `anyOf`, `oneOf` and `allOf` are supported. While the `allOf` objects simply
inherit all properties of the specified schemas, the validation will simply be performed like for any other object
schema.

For `anyOf` and `oneOf` compositions, the created DTO contains specific annotated methods only for validation. With
these methods, it can be validated that the object is valid against exact one schema (`oneOf`) or is valid against at
least one schema (`anyOf`).

REMARK: Currently, an edge case is not supported: While validating against how many schemas the current object is valid
against, only the presence of the required properties are considered. This means, if all required properties of two 
(or more) schemas are present for a `oneOf` composition, but a property of one schema is not valid, the object should be 
considered as valid but it will result in an invalid object as the required properties of two schemas are present.

### Examples
* [OpenAPI spec](example/src/main/resources/openapi-validation.yml)
* [Directory with Tests](example/src/test/java/com/github/muehmar/gradle/openapi/validation)

Samples with Tests for compositions can be found here:
* [OneOf Validation](example/src/test/java/com/github/muehmar/gradle/openapi/oneof/TestValidation.java)
* [AnyOf Validation](example/src/test/java/com/github/muehmar/gradle/openapi/anyof/TestValidation.java)

## Keywords `readOnly` and `writeOnly` 
These keywords for properties are supported. If used, three different DTO's for the same schema are generated:

* Normal DTO containing all properties
* Response DTO containing general and `readOnly` properties
* Request DTO containing general and `writeOnly` properties.

The DTO's are named accordingly, i.e. the normal DTO is named like normal DTO's, the response DTO is suffixed with 
`Response` and the request DTO is suffixed with `Request`. This suffix is added before any configured general suffix,
i.e. if the suffix `Dto` is configured and a schema `Example` contains properties marked as `readOnly` or `writeOnly`, 
then the following DTO's are generated:

* ExampleDto
* ExampleResponseDto
* ExampleRequestDto

## Extraction of enum description

The plugin supports the extraction of description for each member of an enum from the openapi specification. The idea is
to provide an optional default message/description for enums which may be used in the code and are subject to get out of
sync if updated manually.

The assumption is that the description for an enum is provided in form of a list, like the following:

```
  role:
    type: string
    enum: [ "Admin", "User", "Visitor" ]
    description: |
      Role of the user
      * `Admin`: Administrator role
      * `User`: User role
      * `Visitor`: Visitor role
```

If the extraction is enabled, one can define a prefix to let the plugin extract the corresponding description, where the
placeholder `__ENUM__` can be used to match the corresponding member. In this example, the `prefixMatcher` can be set
to `` `__ENUM__`: ``. Everything after the matcher until the line break will get extracted as description for the
corresponding member. The description in the code is available via the `getDescription()` method on the enum.

The configuration setting `failOnIncompleteDescriptions` can be used to prevent missing descriptions for a member cause
of a typo in the enum name (for example if `` * `Vistor`: Visitor role `` is written in the spec) or if one adds a
member without adding the description.

## Parameters

The OpenAPI supports parameters in the `#/components/parameters` section. The plugin will generate for each
parameter a class which contains the constraints of the parameter. For example the specification

```
components:
  parameters:
    limitParam:
      in: query
      name: limit
      required: false
      schema:
        type: integer
        minimum: 1
        maximum: 50
        default: 20
      description: The numbers of items to return.
```

will create the following class

```
public final class LimitParam {
  private LimitParam() {}

  public static final Integer MIN = 1;
  public static final Integer MAX = 50;
  public static final Integer DEFAULT = 20;
  public static final String DEFAULT_STR = "20";

  public static boolean exceedLimits(Integer val) {
    return val < MIN || MAX < val;
  }
}
```

The method `exceedLimits` will contain the conditions depending on the presence of the `minimum` and `maximum`
constraint. In the case both are missing, the method will simply return `false`.

A default number is also rendered as string which may be used as default value in Spring annotations for
parameters.

### Supported schemas

Currently, the following schemas are supported:

* `integer`
    * minimum
    * maximum
    * default

## Incremental build and remote specifications

This plugin supports remote references, i.e. it will also parse any referenced remote specifications and create the
java classes for the schemas in the remote specifications. As the gradle task will depend on this remote specification
files, it must be registered as task-input to properly support incremental build.

The plugin parses by default the given main specification and resolves any referenced remote specifications and register
them as task inputs. This is done before the actual task is executed. This can be disabled (see
the [Configuration](#configuration) section) if needed to avoid parsing the specifications to determine the task inputs.
In case incremental build should still work properly, one has to options:

* In case of no remote reference in the main specification: The main specification is still registered as input,
  therefore incremental build will still work properly.
* In case of remote specifications, one could register the specifications manually as task inputs, like in the following
  example:

 ```
afterEvaluate {
    tasks.named("generateRemoteRefModel") {
        inputs.file("$projectDir/src/main/resources/openapi-remote-ref-sub.yml")
    }
}
 ```

## Credits

* @eikek for the famous `PList`

## Limitations

* The keyword `not` is not supported.
* Multi-Types in v3.1.0 are not supported, i.e. the list in type can contain only one type and optionally the `null` 
  type.
* The object type does not support a combination of `properties`, `additionalProperties` and compositions `allOf`, `oneOf`
  and `anyOf`. An object type can contain only one of the mentioned five keywords (issues `#76`, `#99`, `#100` targeted
  for version 2.0.0).

## Change Log
* 1.1.2 - Fix validation of primitive data types of arrays and maps (issue `#103`)
* 1.1.1 - Fix enum reference in composed pojos (issue `#101`)
* 1.1.0
    * Support OpenAPI spec version 3.1.0 (issue `#60`)
    * Add JavaDoc explanation for deprecated validation methods (issue `#57`)
    * Fix with methods for nullable properties (issue `#70`)
    * Support validation of `multipleOf` constraint (issue `#64`)
    * Support validation of `uniqueItems` constraint (issue `#64`)
    * Support `readOnly` and `writeOnly` keywords (issue `#68`)
    * Remove unused imports in DTO's (issue `#9`)
    * Support root map schemas (issue `#80`)
    * Validate property count constraint for map-properties (issue `#84`)
    * Fix equals, hashCode and toString method for Java-array properties (issue `#83`)
    * Remove empty java-doc tags for empty description (issue `#88`)
    * Add toString method for freeform schemas (issue `#91`)
* 1.0.1
    * Fix issue with property name 'other' (issue `#71`)
    * Fix java keywords as property names and special characters for properties and classes (issue `#72`)
* 1.0.0 - Add support for `anyOf` and `oneOf` (issues `#6` and `#7`)
* 0.22.1 - Fix DecimalMin and DecimalMax imports (issue `#54`)
* 0.22.0
    * Support Free-Form objects (issue `#41`)
    * Support `minProperties` and `maxProperties` constraints (issue `#44`)
    * Support Jakarta Bean Validation 3.0 (issue `#48`)
* 0.21.2 - Fix non Java-String parameters (issue `#38`)
* 0.21.1 - Fix constraints generation for number schemas (issue `#34`)
* 0.21.0
    * Support numeric parameters (issue `#28`)
    * Support string parameters (issue `#29`)
    * Fix exclusiveMaximum and exclusiveMinimum for integer types (issue `#30`)
* 0.20.0
    * Proper release failed, don't use it
* 0.19.0
    * Ignore wrong format for integer or numeric schemas (issue `#25`)
    * Generate simple classes for parameters and their constraints (issue `#24`)
* 0.18.1 - Fix failing excluded external references (issue `#22`)
* 0.18.0
    * Support remote references (issue `#18`)
    * Add possibility to exclude specific schemas from generation (issue `#19`)
* 0.17.0
    * Support customizable builder method prefix (issue `#8`)
    * Improve type mapping configuration (issue `#12`)
* 0.16.0
    * Support for nullability (issue `#3`)
    * Improve exception for enum conversion (issue `#4`)
* 0.15.1 - Support inline object definitions
* 0.15.0 - Support multiple specifications (breaking change in DSL)
* 0.14.1 - Fix issue `#1`
* 0.14.0 - Simplify the format- and class-mapping configuration
* 0.13.2 - Support `allOf` for array items
* 0.13.1 - Quote prefixMatcher to allow special characters
* 0.13.0
    * Add extraction of enum description
    * Fix javadoc rendering
* 0.12.0 - Improve adding optional properties also for the standard Builder
* 0.11.0 - Unreleased (gradle plugin portal problems)
* 0.10.0 - Improve adding optional properties with 'Safe Builder'
* 0.9.1 - Escape patterns for Java
* 0.9.0
    * Create top level enums for root enum definitions
    * Convert enum fields to ASCII java names
    * Fix Java-Bean validation issues
        * Do not use primitive java types to allow checking `@NotNull`
        * Use Java-Bean getter for Booleans (`get` prefix instead of `is`)
* 0.8.0
    * Add support for non-object/non-array schema definitions
    * Convert enums to uppercase snakecase
* 0.7.0 - Add support for `allOf` combinator
* 0.6.0 - Support Java Bean Validation
* 0.5.0
    * Add support for inline object definitions for array items
    * Add support for properties without a type
    * Improve support for maps
* 0.4.0 - Support for inline object definitions
* 0.3.0
    * Add support for enums
    * Fix incremental build
* 0.2.1 - Fix the setter name for booleans
* 0.2.0
    * Support incremental build
    * Add the 'Safe Builder' pattern
    * Extend the supported types/formats
    * Make the JSON support optional
* 0.1.0 - Initial release
