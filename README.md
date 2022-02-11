[![Build Status](https://github.com/muehmar/gradle-openapi-schema/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/muehmar/gradle-openapi-schema/actions/workflows/gradle.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/muehmar/gradle-openapi-schema/blob/master/LICENSE)

# Gradle OpenApi Schema Codegen

This is a gradle plugin to generate Java code given an openapi 3.x specification. Unlike other codegen tools, this
focuses only on the `#/component/schema` section. It generates immutable classes and special builder classes to support
a safe way creating instances. The data classes support JSON conversions via jackson.

* Immutable Java classes.
* Special builder pattern for safe creation of instances.
* JSON support with jackson.
* Customization of the code generation
* Support for Java Bean Validation (JSR 380)
* Extraction of description for enums
* Supports processing multiple specifications

The implementation is based on the
[swagger-parser](https://github.com/swagger-api/swagger-parser)
project.

## Usage

Add the plugin section in your `build.gradle`:

```
plugins {
    id 'com.github.muehmar.openapischema' version '0.15.0'
}
```

## Configuration

Add an `openApiGenerator` block into your `build.gradle` file.

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

            // This would overwrite any global configuration
            enumDescriptionExtraction {
                enabled = true
                prefixMatcher = "`__ENUM__`:"
                failOnIncompleteDescriptions = true
            }

            // Additional format type mapping
            formatTypeMapping {
                formatType = "username"
                classType = "UserName"
                imports = "com.package.UserName"
            }

            // Additional format type mapping
            formatTypeMapping {
                formatType = "password"
                classType = "Password"
                imports = "com.package.Password"
            }

            // Additional class mapping
            classMapping {
                fromClass = "List"
                toClass = "ArrayList"
                imports = "java.util.ArrayList"
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
        classType = "UserName"
        imports = "com.package.UserName"
    }

    // Global format type mapping which gets applied to each schema
    formatTypeMapping {
        formatType = "password"
        classType = "Password"
        imports = "com.package.Password"
    }

    // Global class mapping which gets applied to each schema
    classMapping {
        fromClass = "List"
        toClass = "ArrayList"
        imports = "java.util.ArrayList"
    }
}
```

Add in the `schemas` block for each specification a new block with custom name (`apiV1` and `apiV2` in the example
above) and configure the generation with the following attributes for each schema:

| Key               | Data Type | Default                                    | Description                                                                                             | 
|-------------------|-----------|--------------------------------------------|---------------------------------------------------------------------------------------------------------| 
| sourceSet         | String    | main                                       | Source set to which the generated classes should be added.                                              |
| inputSpec         | String    | None                                       | The OpenApi 3.x specification location.                                                                 |
| outputDir         | String    | $buildDir/generated/openapi                | The location in which the generated sources should be stored.                                           |
| packageName       | String    | ${project.group}.${project.name}.api.model | Name of the package for the generated classes.                                                          |
| suffix            | String    | None                                       | Suffix which gets appended to each generated class. The classes are unchanged if no suffix is provided. |
| jsonSupport       | String    | jackson                                    | Used json support library. Possible values are `jackson` or `none`.                                     |
| enableSafeBuilder | Boolean   | true                                       | Enables creating the safe builder.                                                                      |
| enableValidation  | Boolean   | false                                      | Enables the generation of annotations for java bean validation (JSR 380)                                |

The plugin creates for each schema a task named `generate{NAME}Model` where `{NAME}` is replaced by the used name for
the schema, in the example above a task `generateApiV1Model` and a task `generateApiV2Model` would get created. The
tasks are automatically registered as dependency of the corresponding java-compile task.

### Class Mappings

The plugin allows one to map specific classes to custom types. The following example would use the custom List
implementation `com.package.CustomList` for lists instead of `java.util.List`.

```
classMapping {
    fromClass = "List"
    toClass = "CustomList"
    imports = "com.package.CustomList"
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
    classType = "UserName"
    imports = "com.package.UserName"
}
```

will use the class `com.package.UserName` for the property `accountName`.

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
|------------------------------|-----------|---------|--------------------------------------------------------------------------------------------------------------------------------| 
| enabled                      | Boolean   | false   | Enables the extraction of descriptions for enum from the openapi specification.                                                |
| prefixMatcher                | String    | None    | The prefix which matches the start of the description for the enums.                                                           |
| failOnIncompleteDescriptions | Boolean   | false   | Either no description or a description for each members of an enum must be present if set, otherwise the generation will fail. |

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

## Java Bean Validation

This plugin supports the generation of java bean validation annotations (JSR 380). It requires version 2.0 or above of
the java validation api as dependency. It supports object graph validation via the `@Valid` annotation. The following
annotations from the package `javax.validation.constraints` are currently generated by the plugin:

* `@NotNull` for required properties
* `@Min` and `@Max` for numbers and integers
* `@Size` for strings and arrays
* `@Pattern` for strings
* `@Email` for strings with email format

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

## Credits

* @eikek for the famous `PList`

## Known Issues

* Only `allOf` is supported to combine schemas. Currently, `oneOf`, `anyOf`, and `not` are not yet supported.

## Change Log

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
