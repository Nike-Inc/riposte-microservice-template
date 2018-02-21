# riposte-microservice-template

_Description of your service goes here._

(NOTE: This project was instantiated from the 
[Riposte Microservice Template Kotlin branch](https://github.com/Nike-Inc/riposte%2Dmicroservice%2Dtemplate/tree/projecttemplate/kotlin).
See that template project's readme for some general details on working with a Riposte project.) 

## Information for integrating clients

_Link to API docs and other relevant info for integrating clients goes here._

## Local development

### Service source code

The service's main source code lives in the [riposte-microservice-template-core-code](riposte-microservice-template-core-code)
module.

### Build the service

```bash
./gradlew clean build
``` 

### Run/debug the service locally

The template project's 
["Running the server"](https://github.com/Nike-Inc/riposte%2Dmicroservice%2Dtemplate/tree/projecttemplate/kotlin#running_the_server)
section has full details on all your options. Here's a cheat sheet (**these are all equivalent**).

* **Run/debug directly in your IDE.** For example in IntelliJ you can just right click on the `com/myorg/Main.kt` file and 
select either `Run 'com.myorg.MainKt'` or `Debug 'com.myorg.MainKt'` from the right-click-menu. Selecting the debug option will 
let you hit breakpoints immediately without launching a remote debug session.
    + NOTE: The first time it runs using this launch option it will fail, complaining about the `@appId` and 
    `@environment` System properties. You will need to edit the configuration for this launch option to include the 
    `-D@appId=riposte-microservice-template -D@environment=local` System properties. But you only need to do this 
    once - any later launches will remember these settings.
* **Run w/ Gradle** (no remote debug):

```bash
./gradlew run
```

* **Remote debug w/ Gradle** (remote debug on port 5005):

```bash
./gradlew run --debug-jvm
```

* **Run/remote debug the built fat-jar**, a.k.a. shadow jar (remote debug on port 5005):

```bash
./debugShadowJar.sh
``` 

### Execute remote tests

This project contains "remote tests" that are meant to be executed at the service running somewhere (usually at the
deployed service running in test/prod environment, but you can also point them at your service running locally). These 
remote tests serve as functional tests, integration tests, UATs, smoke tests, or whatever else you want to call them. 
They live in the [riposte-microservice-template-remote-tests](riposte-microservice-template-remote-tests) module,
isolated from the service code.

To execute the remote tests, run the following gradle command: 

```bash
./gradlew functionalTest -DremoteTestEnv=[environment]
```

The value of the `-DremoteTestEnv=[environment]` System property can be `local`, `test`, or `prod`.

Remote tests should not be confused with "Component Tests", which live in the main core-code module and serve as
_compile-time_ integration tests. For more details, see the template project readme sections on 
[Remote Tests](https://github.com/Nike-Inc/riposte%2Dmicroservice%2Dtemplate/tree/projecttemplate/kotlin#remote_tests) and 
[Component Tests](https://github.com/Nike-Inc/riposte%2Dmicroservice%2Dtemplate/tree/projecttemplate/kotlin#component_tests). 
