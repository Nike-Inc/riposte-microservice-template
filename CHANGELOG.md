# Riposte Microservice Template Changelog / Release Notes

All notable changes to the `Riposte Microservice Template` will be documented in this file. This is a template project 
and is not available via typical artifact repositories (Maven Central, etc), therefore it does not have version 
numbers. This file will track changes based on the dates the changes were made.

## 2022-03-15

### Updated

- Upgraded most libraries to the latest available.
- Upgraded tests to JUnit 5.

### Build

- Upgraded to gradle 7.4.1.
- Migrated from Travis CI to Github Actions for the CI build.
- Removed jcenter references.

## 2020-07-13

### Updated

- Upgraded most libraries to the latest available.

### Removed

- Removed all cassandra-related example stuff, as it caused the project to be excessively bloated.
- Removed the Eureka dependency by default because it pulls in a lot of dependencies, and many projects don't need it.
You can re-enable Eureka by searching the project for the `TODO: EXAMPLE CLEANUP` markers and following instructions.
- Removed the groovy dependency after migrating to XML-based logback configuration.
- All of the above dependency removals and cleanup resulted in the application fat jar dropping from 74 MB 
down to 15 MB, and reduced build time by about 50-60%.

### Changed

- Changed from groovy-based to XML-based logback config. This made it possible to remove the groovy dependency entirely,
and improves app startup time.
- Moved metrics, security, and eureka initialization to their own guice modules.
- Changed the `debugActionsEnabled` property to false for local config. This removes logging for all the System and 
application properties on startup.
- Changed the unit test gradle output to only log skipped or failed test events.
- Cleaned up miscellaneous code warnings.

## 2019-08-29

### Updated

- Upgraded most libraries to the latest available.
- Generally cleaned up, updated, and modernized the project.

## 2018-02-22

### Added

- Added dependency on `com.google.code.findbugs:jsr305` - this ensures Guice can see `@Nullable` annotations after 
removing example code. Previously `@Nullable` was being transitively pulled in by other dependencies that aren't 
needed for all projects (e.g. Eureka dependencies).
- Added a unit test to cover `ProjectApiError.getMetadata()` so that code coverage stays at 100% after removing 
example code.

### Fixed

- Fixed `bootstrap_template.sh` to run the `./gradlew` command as a background task to prevent gradle from squashing 
stdout/stderr, which was preventing final success/failure messages from reaching stdout/stderr.

## 2018-02-21

### Updated

- Updated Jackson dependencies to explicitly specify version `2.9.4` for all modules that were being pulled in 
transitively.

### Other

- Moved endpoint matchers to constants to prevent unnecessary object creation.
- Added to the manual-error-throwing example in `ExampleEndpoint.Post` to show how you can specify extra logs and/or
response headers when throwing an `ApiException`.
- Cleaned up various code warnings.
- Removed a test that was there for code coverage which became unnecessary with the upgrade to Jacoco 0.8.0. 
- Force gradle to always execute the `functionalTest` task when specified, even if no code changes have occurred.

## 2018-02-20

### Updated

- Updated various libraries:
    - Riposte libraries from `0.10.0` to `0.12.0` ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
    - Backstopper libraries from `0.11.1` to `0.11.4` ([Backstopper changelog](https://github.com/Nike-Inc/backstopper/blob/main/CHANGELOG.md)).
    - SLF4J libraries from `1.7.21` to `1.7.25` ([SLF4J changelog](https://www.slf4j.org/news.html)).
    - Logback libraries from `1.1.7` to `1.2.3` ([Logback changelog](https://logback.qos.ch/news.html)).
    - AssertJ libraries from `3.5.2` to `3.9.0` ([AssertJ changelog](http://joel-costigliola.github.io/assertj/assertj-core-news.html)).
    - RestAssured libraries from `2.3.3` to `3.0.7` ([RestAssured changelog](https://github.com/rest-assured/rest-assured/blob/master/changelog.txt)).
    - Jacoco from `0.7.7.201606060606` to `0.8.0` ([Jacoco changelog](http://www.jacoco.org/jacoco/trunk/doc/changes.html)).
    - Gradle from `2.13` to `4.5.1` ([Gradle releases](https://gradle.org/releases/)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

### Other

- Other changes:
    - Move local-override properties file creation to `processResources` gradle task.
    - Allow bootstrapping projects into empty git repos (directories that only have a `.git` subdirectory and nothing 
    else).
    - Done by [Nic Munroe][contrib_nicmunroe].

## 2017-07-17

### Fixed

- Fixed bootstrap script to work with versions of `tar` that don't automatically detect gzip.
    - Fixed by [amitsk][contrib_amitsk] in [pull request #11](https://github.com/Nike-Inc/riposte-microservice-template/pull/11).

## 2017-05-23

### Updated

- Updated Riposte libraries to 0.10.0 ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

## 2017-04-28

### Updated

- Updated Riposte libraries to 0.9.4 ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

## 2017-04-20

### Updated

- Updated Riposte libraries to 0.9.3 ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

## 2017-02-28

### Updated

- Updated Riposte libraries to 0.9.0 ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
- Updated Backstopper libraries to 0.11.1 ([Backstopper changelog](https://github.com/Nike-Inc/backstopper/blob/main/CHANGELOG.md)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

## 2017-01-17

### Fixed

- Fixed link to future-converter library in readme.
    - Fixed by [Lukáš Křečan][contrib_lukas-krecan].

## 2016-12-12

### Updated

- Updated Riposte libraries to 0.8.2 ([Riposte changelog](https://github.com/Nike-Inc/riposte/blob/main/CHANGELOG.md)).
- Updated Backstopper libraries to 0.11.0 ([Backstopper changelog](https://github.com/Nike-Inc/backstopper/blob/main/CHANGELOG.md)).
    - Updated by [Nic Munroe][contrib_nicmunroe].

## 2016-11-08

### Added

- Initial open source code drop for the Riposte Microservice Template.
	- Added by [Nic Munroe][contrib_nicmunroe].
	

[contrib_nicmunroe]: https://github.com/nicmunroe
[contrib_lukas-krecan]: https://github.com/lukas-krecan
[contrib_amitsk]: https://github.com/amitsk
