# Riposte Microservice Template - Java

[![][travis img]][travis]
[![Code Coverage](https://img.shields.io/codecov/c/github/Nike-Inc/riposte-microservice-template/master.svg)](https://codecov.io/github/Nike-Inc/riposte-microservice-template?branch=master)
[![][license img]][license]

**This is an example template for quickly creating a new Java-based [Riposte](https://github.com/Nike-Inc/riposte) project.** 
Riposte is a Netty-based microservice framework for rapid development of production-ready HTTP APIs. It includes robust 
features baked in like distributed tracing (provided by the Zipkin-compatible 
[Wingtips](https://github.com/Nike-Inc/wingtips)), error handling and validation (pluggable implementation with the 
default provided by [Backstopper](https://github.com/Nike-Inc/backstopper)), and circuit breaking (provided by 
[Fastbreak](https://github.com/Nike-Inc/fastbreak)). 

***IMPORTANT NOTE:*** Riposte uses Java 8. This project will not build or run unless you use a Java 8 JDK. Verify 
you're using a Java 8 JDK with a simple `java -version`.

## Want a Kotlin Version of this Microservice Template?

You're currently viewing the Java version of this Riposte microservice template. **A Kotlin version exists - if you 
want to create a Kotlin-native Riposte project please see the 
[Kotlin branch](https://github.com/Nike-Inc/riposte-microservice-template/tree/projecttemplate/kotlin) of this repository.**

<a name="tldr"></a>
## TL;DR - Getting Started

* Open a command line shell and `cd` into the location you want for your new Riposte project.
* Run the following bootstrapping `curl` command, replacing `newprojectname` with the new project name you want, and 
replacing `myorgname` with the name of your company/org (this is used for package names, i.e. `com.myorgname`):
``` shell
curl -s 'https://raw.githubusercontent.com/Nike-Inc/riposte-microservice-template/master/bootstrap_template.sh' \
| bash /dev/stdin newprojectname myorgname
```
* `cd` into the `newprojectname` folder.
* Build and run the new project: `./gradlew clean build run` 
	- See the [running the server](#running_the_server) section for more run options, including starting up directly in 
	your IDE.
* Hit an example endpoint, like [http://localhost:8080/example](http://localhost:8080/example). 
	- See [the examples section](#example_endpoints) for info on all the example endpoints.
* To run the functional tests execute the following gradle command: 
`./gradlew functionalTest -DremoteTestEnv=[environment_id]` where `[environment_id]` is one of the following: local, 
test, or prod. 
	- See the [remote tests submodule](#remote_tests) section for more detailed info on the functional tests. 

<a name="top"></a>
### Table of Contents

* [TLDR; Getting Started](#tldr)
* [Bootstrapping New Projects](#how_to_generate_new_project)
	* [Automated bootstrapping](#automated_bootstrapping)
	* [Manual bootstrapping](#manual_bootstrapping)
	* [Bootstrap command arguments](#bootstrap_command_args)
	* [Setting environment-specific properties during bootstrapping](#set_env_props_during_bootstrap)
	* [Example all-in-one curl command](#example_all_in_one_curl_command)
* [Running the server](#running_the_server)
    * [Disabling embedded cassandra](#disabling_embedded_cassandra)
* [Example endpoints](#example_endpoints)
    * [Other things to try](#other_things_to_try)
* [Template Application properties and dependency injection](#app_props_and_dependency_injection)
	* [Application properties (Typesafe Config)](#app_props_info)
		* [What if I don't want to use Typesafe Config?](#want_different_app_prop_system)
	* [Dependency injection (Guice)](#guice_info)
		* [What if I don't want to use Guice?](#do_not_want_guice)
* [Building endpoints](#building_endpoints)
	* [How to build endpoints? Blocking or non-blocking?](#blocking_vs_nonblocking)
	* [More on building non-blocking endpoints](#more_on_nonblocking)
	* [Project errors and error handling](#error_handling)
	* [Incoming request content automatic validation](#request_content_validation)
	* [Java Bean Validation (JSR 303) on arbitrary objects](#arbitrary_jsr303_validation)
* [Metrics](#metrics)
* [Remote tests submodule](#remote_tests)
* [Component tests](#component_tests)
* [Removing the example code](#removing_example_code)
* [License](#license)

<a name="how_to_generate_new_project"></a>
## Bootstrapping New Projects

Bootstrapping new Riposte projects is straightforward - you can do it in an automated fashion with a one line command 
and you don't even need to checkout the template project repository. If that doesn't work in your environment you can 
do the manual method that has a few more steps but is also straightforward.

<a name="automated_bootstrapping"></a>
### Automated bootstrapping

Just run the following `curl` command in a command line shell, replacing and/or removing the `<newprojectname>`, 
`<myorgname>`, `</optional/target/dir>`, and `<-DoptionalSystemProps=stuff>` arguments as necessary (arguments and 
options explained [below](#bootstrap_command_args)):

``` shell
curl -s 'https://raw.githubusercontent.com/Nike-Inc/riposte-microservice-template/master/bootstrap_template.sh' \
| bash /dev/stdin <newprojectname> <myorgname> </optional/target/dir> <-DoptionalSystemProps=stuff>
```

After you execute the `curl` command your new project will be setup and ready to use.

<a name="manual_bootstrapping"></a>
### Manual bootstrapping

If the `curl` command above doesn't work for you then you will need to perform a few more steps to setup your project:

* Download the following archive of this template project repository: 
[https://github.com/Nike-Inc/riposte-microservice-template/archive/master.zip](https://github.com/Nike-Inc/riposte-microservice-template/archive/master.zip)
* Unpack this zipped archive wherever you want your project to live.
* Open a command line shell and `cd` into the new project folder that was just unpacked.
* Execute the following gradle wrapper command, replacing and/or removing the `<newprojectname>`, `<myorgname>`, and 
`<-DoptionalSystemProps=stuff>` arguments as necessary (arguments and options explained 
[below](#bootstrap_command_args)):
``` shell
./gradlew replaceTemplate -DnewProjectName="<newprojectname>" -DmyOrgName="<myorgname>" \
-DallowDashes=true <-DoptionalSystemProps=stuff>
```

When the `gradlew` command finishes your new project will be setup and ready to use.

<a name="bootstrap_command_args"></a>
### Bootstrap command arguments

* `<newprojectname>` - REQUIRED - The name of the new project.
* `<myorgname>` - REQUIRED - The company/organization name to use - this is used for package naming, 
e.g. `com.myorgname`.
* `</optional/target/dir>` - OPTIONAL - The path to the directory where the template project should be checked out and 
renamed. Defaults to `<newprojectname>` if not specified.
* `<-DoptionalSystemProps=stuff>` - OPTIONAL - A series of -D Java System Property flags that will get passed to the 
`setup.groovy` script during the renaming process for renaming optional environment-specific properties. See the 
section on [setting environment-specific properties during bootstrapping](#set_env_props_during_bootstrap) below for 
full details of what you can send and what each one does.

<a name="set_env_props_during_bootstrap"></a>
### Setting environment-specific properties during bootstrapping

There are some project-specific config properties in this template project. You can set them up manually after 
bootstrapping (search for `fixme_` in the project), or you can enrich the bootstrapping commands above with extra info 
(the `<-DoptionalSystemProps=stuff>` properties described above) and have those properties set at bootstrapping time 
when the project is first created. This is great for repeatability and rapid iteration.

The following table describes the System Property values you can pass in with the automated or manual bootstrap command 
for the `<-DoptionalSystemProps=stuff>` argument(s) and what they do. For each `prop_key` defined below that you want 
to specify you would pass in `-Dprop_key=value`. See the 
[Example all-in-one curl command](#example_all_in_one_curl_command) after this table for a concrete example of these 
properties in action. **All of these are optional** - any you don't specify will just have the `fixme_*` value left in 
the configs, and since they just relate to Eureka and remote testing in test and prod environments it won't affect your 
ability to explore, build, or run your project locally.

| System Property Key | Explanation |
| -------------: | :------------- |
| fixme_project_remotetest_url_test | The URL to your server(s) deployed in the test environment, e.g. `https://myproject.test.myorg.com`. Used for executing the remote functional tests against the test environment. |
| fixme_project_remotetest_url_prod | Same as above, but for prod environment. |
| fixme_eureka_domain_test | The domain name of the Eureka server in your test environment, e.g. `eureka.test.myorg.com`. You can safely ignore this if you don't use Eureka. |
| fixme_eureka_domain_prod | Same as above, but for prod environment. |

<a name="example_all_in_one_curl_command"></a>
#### Example all-in-one `curl` command

The following `curl` command is an example for a project named `example-riposte-project`:

``` shell
curl -s 'https://raw.githubusercontent.com/Nike-Inc/riposte-microservice-template/master/bootstrap_template.sh' \
| bash /dev/stdin example-riposte-project someorg \
-Dfixme_project_remotetest_url_test=https://exampleriposteproject.test.someorg.com \
-Dfixme_project_remotetest_url_prod=https://exampleriposteproject.someorg.com \
-Dfixme_eureka_domain_test=eureka.test.someorg.com \
-Dfixme_eureka_domain_prod=eureka.someorg.com
```

[back to top](#top)

<a name="running_the_server"></a>
## Running the server

A Riposte application is ultimately just a simple standard `public static void main` style java app. No container to 
deal with, no funky setup requirements. The main class is `com.myorg.Main`. The only thing you have to do when 
launching the app is to set the following System properties: `@appId` and `@environment`. By default this app uses 
Archaius-style conventions for property/environment management, and it needs those two System properties to know which 
`src/main/resources/*.properties` and/or `src/main/resources/*.conf` files to load (this template actually uses 
Typesafe Config under the hood by default, not Archaius, but the Archaius naming conventions are useful and allow you 
to trivially switch to Archaius if you want). `@appId` is the name of your project (i.e. the `rootProject.name` you set 
in settings.gradle), and `@environment` is the environment you're running in (i.e. local, test, or prod).

For example if your project is named `foo` and you're running on your local box then you'd set 
`-D@appId=foo -D@environment=local` for your System properties when starting the server (see 
[the properties section](#app_props_and_dependency_injection) for more information on how the Archaius-style 
conventions work).

There are three out-of-the-box ways to launch the app:

1. A simple method for launching the app during development is directly in your IDE. Depending on your development 
style this may be the most efficient launch method for rapid iteration. For example in IntelliJ you can just right 
click on the `com.myorg.Main` class and select either `Run 'Main.main()'` or `Debug 'Main.main()'` from the 
right-click-menu. Selecting the debug option will let you hit breakpoints immediately without launching a remote debug 
session.

	**NOTE:** The first time it runs using this launch option it will fail, complaining about the `@appId` and 
	`@environment` System properties. You will need to edit the configuration for this launch option to include the 
	`-D@appId=foo -D@environment=local` System properties. But you only need to do this once - any later launches will 
	remember these settings.

2. The gradle build file is setup with the application plugin, so if you want to launch from the command line you can 
perform the following command: `./gradlew run`. It is already configured with the proper `@appId` and `@environment` 
System properties for local development so it should just work.

	**NOTE:** If you want to do remote debugging you'll need to launch it with the `--debug-jvm` flag, 
	e.g.: `./gradlew run --debug-jvm`. This will cause the server to pause on startup until you connect a remote debug 
	session on port 5005.

3. When the gradle build runs, it creates a "shadow jar" (a.k.a. "fat jar") at 
`[projectroot]/build/libs/[appId]-[version].jar`. This shadow jar is a single executable jar file that contains the 
entire application, including third party libraries. The only classpath it needs is itself, so to launch the 
application using this shadow jar you would do something like the following:

	`java -jar -D@appId=foo -D@environment=local build/libs/*.jar`

	**This is usually how you would want to launch a Riposte app on a production server.** Note that you can add 
	standard JVM args to this command to support remote debugging, change memory or garbage collection options, etc. 
	There is a `debugShadowJar.sh` script at the root of the project that already contains this command and configures 
	remote debugging on port 5005.

<a name="disabling_embedded_cassandra"></a>
### Disabling embedded Cassandra

This template microservice contains an embedded Cassandra database as an example of interacting with a database in an 
async nonblocking way. Unfortunately this significantly increases the startup time of the application. When you're done 
experimenting with the Cassandra endpoint you can disable embedded Cassandra so that subsequent startup times are much 
quicker. Simply add the following line to your `[appname]-core-code/src/main/resources/[appname]-local-overrides.conf` 
file: `disableCassandra=true`. 

[back to top](#top)

<a name="example_endpoints"></a>
## Example endpoints

The following example endpoints are available in this template project. By default they are available at 
`http://localhost:8080/[path]`. It's recommended that you use a REST client like [Postman](https://www.getpostman.com/) 
for making the requests so you can easily specify HTTP method, payloads, headers, etc, and fully inspect the response:

* `GET|POST /example` - Basic GET/POST behavior with validation. The GET call just returns an example payload. You can 
copy/paste this payload and POST it back to explore the validation and exception behavior. The `input_val_1` and 
`input_val_2` fields are required and validation is controlled by JSR 303 annotations - remove these fields or make 
them blank for your `POST` call to see the validation in action. Set the `throwManualError` field to true to see the 
result of a thrown exception from inside an endpoint. Implemented by the `ExampleEndpoint.Get` and 
`ExampleEndpoint.Post` classes.
* `ANY-METHOD /exampleDownstreamHttpAsync` - An example of performing a downstream HTTP network call using an 
async/nonblocking NIO client to avoid using any threads for the entirety of the endpoint. Since we want the example 
endpoints to be fully self-contained we simply make a downstream call to our own `/example` endpoint described above. 
To show how you can do additional work on the response from the downstream call we insert a 
`"viaAsyncHttpClient": "true"` JSON field into the downstream response before returning. You can inspect the logs to 
see two requests to the server for each single request from an outside caller. Implemented by the 
`ExampleDownstreamHttpAsyncEndpoint` class.
* `ANY-METHOD /exampleProxy` - An example of using Riposte's proxy/router endpoint feature. These endpoints allow you 
to match an incoming request and define where the downstream call goes, optionally adjusting query params, headers, etc 
before sending to the downstream target. The response from the downstream system will be automatically piped back to 
the caller. This all happens in the background with the HTTP request/response chunks being streamed, keeping memory 
usage level and lag time minimal even while streaming gigabyte payloads, and the async/nonblocking NIO keeps thread 
usage static as well. In this case this proxy endpoint works similarly to `/exampleDownstreamHttpAsync` in that the 
downstream system is our own `/example` endpoint in order to keep the example project fully self-contained. You can 
inspect the logs to see two requests to the server for each single request from an outside caller. Implemented by the 
`ExampleProxyRouterEndpoint` class.
* `ANY-METHOD /exampleCassandraAsync` - An example of using Cassandra's async driver to do database queries without 
using any threads. Implemented by the `ExampleCassandraAsyncEndpoint` class.
* `GET|POST /exampleBasicAuth` - A set of example endpoints that show Riposte's security validation system in action. 
The `POST` endpoint is protected by basic auth, so if you call it without the proper auth header you will get a 401 
error. The `GET` endpoint is *not* protected and you can call it without any auth header. The response you get will be 
a JSON object with a description of the auth header you can add in order to successfully to call the protected 
`POST /exampleBasicAuth` endpoint. Implemented by the `ExampleBasicAuthProtectedEndpoint.Get` and 
`ExampleBasicAuthProtectedEndpoint.Post` classes.

Note that in a real production application you might want to protect all endpoints except `/healthcheck`. For the 
examples above only `POST /exampleBasicAuth` is protected. See the comments and implementation of 
`AppGuiceModule.basicAuthProtectedEndpoints(Set)` to see how to switch to protect all endpoints except `/healthcheck`.

<a name="other_things_to_try"></a>
### Other things to try

In addition to the example endpoints there is some core Riposte functionality to investigate:

* Trigger a 404 by making a request to a path that does not exist like `/foobar`.
* Trigger a 405 by making a request to a path that exists but using a HTTP method that is not allowed for that path, 
like `DELETE /example`.
* When triggering errors (including validation or intentional errors from the example endpoints) make sure you copy the 
error_id from the response and search for it in the app logs to see how easy it is to correlate individual errors with 
the single log message that contains all the debugging info about the request and error. Different error types may 
contain different information relevant to that particular error.
* Every request, whether it is an error or not, will include a `X-B3-TraceId` response header. You can copy this and 
search for it in your app logs. *Every* log message that was output for that request will be tagged with that trace ID, 
making it trivial to find all the logs associated with a given request.

[back to top](#top)

<a name="app_props_and_dependency_injection"></a>
## Template Application properties and dependency injection

<a name="app_props_info"></a>
### Application properties (Typesafe Config using Archaius conventions)

By default the template application is setup to use Typesafe Config with Archaius-style conventions for property 
file/environment management. If you look in `[projectroot]/[appId]-core-code/src/main/resources` you'll see several 
`*.conf` files. When the app server is launched it requires two System properties to be set: `@appId` and 
`@environment`. These System properties are used by Typesafe Config to determine which properties files to load. 
`[appId].conf` is always loaded - it acts as the default set of properties. The other properties files are named in the 
format `[appId]-[environment].conf`, so after loading the default properties file Typesafe Config will use the 
`@environment` System property to construct the correct properties filename for the environment you're currently in and 
load that file as an addition and override of the default properties file.

Whenever there are property name collisions with the default and environment-specific properties files, the 
environment-specific ones will win since they are loaded after the default properties file. You can also add new 
properties in the environment file that don't exist in the default file and they will be loaded into Typesafe Config as 
well when that environment is specified. NOTE: You can pass in System properties to the application and they will be 
available for use in Typesafe Config, and System properties will override file-based properties if they have the same 
name.

You may notice there is one properties file that doesn't follow this `[appId]-[environment].conf` convention: 
`[appId]-local-overrides.conf`. If you look in `[appId]-local.conf` you'll see the last line says: 
`include "[appId]-local-overrides.conf"`. This tells Typesafe Config to load the *local-overrides* properties file 
after it loads the *local* properties file. Since the local-overrides properties file is in `.gitignore` it is ignored 
by git which allows you to setup any temporary custom configuration for your local box without worrying about modifying 
anything that is checked into git.

<a name="want_different_app_prop_system"></a>
#### What if I don't want to use Typesafe Config?

The way Typesafe Config is used provides an easy and convenient way to have environment-specific properties, and we 
have a helper Guice module that knows how to extract the Typesafe Config properties and register them with Guice so you 
can inject property values into your code trivially without any setup (see the Guice section below). That said, you are 
not required to use Typesafe Config in your project. To replace it with something else you would need to do the 
following:

1. If you want to use Archaius instead, change your `Main` class from extending `TypesafeConfigServer` to 
`ArchaiusServer` (found in the `com.nike.riposte:riposte-archaius` dependency). If you don't want Typesafe Config or 
Archaius you can write your own class that mimics what `TypesafeConfigServer` and `ArchaiusServer` do but have your own 
property loading strategy.
2. By default Guice expects to find certain properties from your properties files registered with it so that they can 
be injected into `GuiceProvidedServerConfigValues` (which is used by the template application's `AppServerConfig` 
class). This is done by passing a `PropertiesRegistrationGuiceModule` to `AppServerConfig`, and by default a module 
that extracts the properties from Typesafe Config is used. So if you remove Typesafe Config you'll need to either:

	a. Create `AppServerConfig` with a different `PropertiesRegistrationGuiceModule` that knows about your 
	application's properties, and make sure those properties include all the config bits expected by 
	`GuiceProvidedServerConfigValues` (see `[appId].conf` for the recommended default values).
	
	b. ***--OR--*** Modify your `AppGuiceModule` to expose all the necessary config bits manually as `@Named` 
	injectable beans. To create this manually in one of your Guice modules you'd do something like:

			@Provides
			@Singleton
			@Named("endpoints.port")
			public int endpointsPort() { return 4242; }

	c. ***--OR--*** A combination of the above. Again, Guice just needs to be able to inject everything marked 
	`@Inject` in `GuiceProvidedServerConfigValues`. How you set it up is up to you, so you could have some of them 
	provided via `PropertiesRegistrationGuiceModule` and others via manual bean definition.

Of course, if you *also* rip out Guice (see below) and start the app using your own custom implementation of 
`ServerConfig` then some of the above won't be necessary, but you're on your own at that point. Ultimately all you have 
to do is figure out how to pass a valid instance of `ServerConfig` to the Riposte server when it is created in 
`com.myorg.Main`.

<a name="guice_info"></a>
### Dependency injection (Guice)

By default this template application uses Guice for dependency injection. The guts of Riposte do not use dependency 
injection and get all the configuration and objects needed to run the server infrastructure through the `ServerConfig` 
that is passed into the server when it is created. But since the `ServerConfig.appEndpoints()` method provides the 
endpoint objects that will be registered with the server, and those endpoints are where the vast bulk of your 
application will reside, all you have to do is make sure your endpoints are Guice enabled and your app will effectively 
be fully dependency-injection-capable.

`AppGuiceModule` is the main Guice module for the application. It exposes the `@Named("appEndpoints")` bean that 
returns the list of endpoints for your app to use, so whenever you create a new endpoint just make sure you add it to 
the argument list for this method and return it in the return list for the method, and your endpoint will have full 
dependency injection support (by adding it to the argument list for the method Guice will auto-create an instance for 
you and perform all the requested injection in that class).

Other potentially interesting info regarding how this application's Guice setup is done:

* By default `AppServerConfig` uses a `TypesafeConfigPropertiesRegistrationGuiceModule`. This module is auto-added to 
the Guice modules used by the app when it starts up, and causes all the Typesafe Config properties to be registered 
with Guice so that they can be injected by simply adding `@Inject @Named("my.prop.key")` annotations to the 
field/argument you want the property value injected into.
* `AppServerConfig.getAppGuiceModules(Config)` specifies all the other modules that should be available to Guice. This 
includes the main `AppGuiceModule`, but it also includes `BackstopperRiposteConfigGuiceModule`, which is responsible 
for wiring up the default error handling and validation implementations into the application. If you want other custom 
modules to be available to your app you can add them to the list returned by 
`AppServerConfig.getAppGuiceModules(Config)`.

<a name="do_not_want_guice"></a>
#### What if I don't want to use Guice?

You're not required to use Guice in your application. The only strict requirement is that your application passes a 
valid `ServerConfig` into the Riposte server when it is created in `com.myorg.Main`. `ServerConfig` is just an 
interface, so you're free to use other dependency injection implementations if you want to wire up your endpoints, and 
as long as your `ServerConfig` returns the wired-up objects you're good to go.

And if you don't like dependency injection at all just rip it out. Again, as long as you provide a valid `ServerConfig` 
implementation to the Riposte server when it is created in `com.myorg.Main` it doesn't matter what technologies you do 
or don't use under the hood.

[back to top](#top)

<a name="building_endpoints"></a>
## Building endpoints

Building endpoints is fairly straightforward. Just add classes that extend `StandardEndpoint` or `ProxyRouterEndpoint` 
and make sure they are returned via the `AppGuiceModule.appEndpoints(...)` method. The `StandardEndpoint` and 
`ProxyRouterEndpoint` base classes define abstract methods that you have to override, and the Riposte server uses those 
methods to route requests to the correct endpoints and execute the endpoint on the incoming request when appropriate.

See the `Example*Endpoint` classes for examples of both the standard non-blocking and proxy-style endpoints, and 
examples of using the error handling and validation system.

***Don't forget to add new endpoint classes to `AppGuiceModule.appEndpoints(...)` or they will not be registered with 
the server and you'll get 404 errors when trying to hit them!***

<a name="blocking_vs_nonblocking"></a>
### How to build endpoints? Blocking or non-blocking?

Even though the `StandardEndpoint` endpoints are inherently geared toward being used in a non-blocking style, it is 
possible to build endpoints in a blocking way by setting up the returned `CompletableFuture` to use the 
`longRunningTaskExecutor` to spin up a thread and do blocking stuff in that thread (e.g. calling a database or 
downstream system and waiting in the thread for the response). This is fine for a quick proof of concept app or if 
there's simply no other way to do it, but if at all possible you'll want to do things in a non-blocking style by using 
async non-blocking drivers for database calls, downstream HTTP calls, etc, that return a future.

Why? Blocking-style endpoints have the performance characteristics of traditional thread-per-request synchronous 
Servlet-based applications. This means every concurrent request requires a new thread that sits around until the 
blocking work is done, and when you have too many threads going at once you'll take a noticeable performance hit due to 
context switching. The `longRunningTaskExecutor` is (by default) set up to be an unlimited thread pool where new 
threads are spun up as necessary and reclaimed after 60 seconds of being idle, so at least you wouldn't need to 
manually fiddle with thread pool sizes, but at the same time under load it would have the general performance 
characteristics of a blocking Servlet-based app where the app may fall over long before CPU, memory, and other 
resources have been used up on the machine. By building things in non-blocking style using proper async non-blocking 
drivers (e.g. for database and downstream HTTP calls) so that thread counts do *not* increase as more concurrent 
requests enter the system, you'll find that the app is able to fully utilize the CPU/memory/network/etc resources of 
the machine before performance drops.

Of course sometimes extra threads are non-negotiable. For example if you have to do complex calculations that take a 
long time, those calculations have to be executed somewhere, and in these cases you will need to use the 
`longRunningTaskExecutor` to spin up threads to do the work (or use your own custom thread pool). But for anything that 
interfaces with another application on the machine (e.g. database calls on the local box) or another system entirely 
(e.g. downstream HTTP calls to another server) there is often a non-blocking driver that doesn't require extra threads.

***TLDR:*** Waiting for downstream systems to do work? Use an async nonblocking driver to prevent extra threads. Doing 
serious crunching in the app server itself? You'll need to spin up an extra thread (use the `longRunningTaskExecutor`). 
See below for more info on how to identify the best way to build a given endpoint.

<a name="more_on_nonblocking"></a>
### More on building non-blocking endpoints

The `StandardEndpoint` endpoints are implementation-agnostic. Their `execute` method returns a `CompletableFuture`, 
which just means the endpoint is saying "I'll finish what I'm doing at some point in the future and will be ready to 
give you the response to send to the client at that time". And since it's a `CompletableFuture`, Riposte simply 
registers a listener on that future so it gets automatically notified when the job is done and will send the response 
at that point. `CompletableFuture`s are composable so you can parallelize your work.

***IMPORTANT NOTE:*** It's up to the `CompletableFuture` to handle threading issues. This means you can get yourself in 
trouble if you're not careful. ***Carefully read the javadoc on the 
`NonblockingEndpoint.execute(RequestInfo, Executor, ChannelHandlerContext)` method to familiarize yourself with some of 
the pitfalls and best practices***, but in general the rules are:

* If the work you need to do is trivial and likely measured in nanoseconds or microseconds (seriously, even 
milliseconds might be too long for high traffic servers), go ahead and use the synchronous methods to create and 
compose your `CompletableFuture` or allow it to use the default `Executor` for the async methods (which uses the 
default JVM fork-join pool under the hood and only has a handful of threads, so again be careful with this and make 
sure the work is truly trivial). You can even do the work in the `execute` method itself and use 
`CompletableFuture.completedFuture()` to create an already-finished `CompletableFuture` to pass back.
* Otherwise you need to run your task in such a way that it won't suffer from or cause bottlenecks. You have several 
options, and each one is useful in different situations - there is no one-size-fits-all solution if you want maximum 
performance!
	1. If the task you need to do has an async non-blocking solution that doesn't eat up a bunch of threads when a lot 
	of concurrent requests are happening, then use that.
		* For example Cassandra has an async driver that can handle Cassandra requests concurrently with only a few 
		threads, so if you're doing Cassandra stuff use that (see `ExampleCassandraAsyncEndpoint` for an example of 
		using that driver).
		* Similarly if you're making downstream HTTP calls you can leverage Netty to do it in an efficient async 
		nonblocking way without spawning a new thread to make the call (see `ExampleDownstreamHttpAsyncEndpoint` for an 
		example of this).
	2. If there's no way to do the task in a low-thread-count-for-many-requests async nonblocking way then you'll need 
	to use a separate thread for the task. The `StandardEndpoint.execute()` method is passed an `Executor` intended to 
	be used as the "kitchen sink" for this purpose. By default this `Executor` is unbounded and will create threads as 
	necessary. Those threads will be reused if idle, and any thread idle for more than 60 seconds is reclaimed. To use 
	this `Executor` just remember that you need to use the `CompletableFuture.*async()` method signatures that allow 
	you to pass the `Executor` you want used as the last argument in the method call. If you use the versions of the 
	`CompletableFuture.*async()` methods that don't take an `Executor` then it will use the default JVM fork-join pool, 
	which is usually not what you want unless the task is measured in a few milliseconds.
	3. If you don't want to use the default `Executor` passed into the non-blocking `execute` method then you are free 
	to use your own custom `Executor` to fully control the threading behavior. Just make sure you understand what 
	you're doing and why - it's easy to hamstring yourself by accident.

As you are composing your `CompletableFuture`s you may find yourself stuck with similar future-type objects that aren't 
directly compatible. For example Google Guava's `ListenableFuture`. It's a very similar object, but you can't return it 
directly from a `StandardEndpoint.execute()` method call. In these cases you can transform these other structures into 
a `CompletableFuture` using a simple library. In the Guava `ListenableFuture` example you just need to pull in the 
`net.javacrumbs.future-converter:future-converter-java8-guava` dependency and call the 
`FutureConverter.toCompletableFuture(ListenableFuture)` method. Similar libraries are available from the same developer 
for converting RxJava's `Observable` and for converting Spring's `ListenableFuture`. See 
[the developer's page](https://github.com/lukas-krecan/future-converter) for details.

<a name="error_handling"></a>
### Project errors and error handling

Error handling and validation is the same no matter which endpoint type you use. For errors, the template app is wired 
up with [Backstopper](https://github.com/Nike-Inc/backstopper) via the `BackstopperRiposteConfigGuiceModule` Guice 
module registration (see `AppServerConfig`). It is designed to guarantee that any error will be shown to the user in 
the same error contract, with all errors matching up with one of the enum values in `SampleCoreApiError` or your 
application's `ProjectApiError`. If the error handler doesn't recognize the exception it will generate a generic 500 
error for the user (mapped from `SampleCoreApiError.GENERIC_SERVICE_ERROR` if you want to see the code and message sent 
to the user). If it does recognize the exception it will intelligently convert it to one or more of the 
`SampleCoreApiError` or `ProjectApiError` enum values, which are in turn converted into the error contract for the 
user.

The main way to manually throw an error in the application so that it will be handled the way you want is to throw a 
`com.nike.backstopper.exception.ApiException`. There's a builder for this exception that lets you specify the 
`ApiError` instances you want returned to the user (`ApiError` is the interface that `SampleCoreApiError` and 
`ProjectApiError` implement), and you can also include the exception message you want, any `Throwable` that caused the 
error, and a list of extra key/value pairs that you want to be logged along with the error when it is handled. It is 
also possible to add some extra dynamic metadata to the error contract shown to the user by wrapping one or more of 
the `ApiError` instances with `new ApiErrorWithMetadata(originalApiError, metadataMap)`.

The error handling system is also linked with the validation system to make translating from validation errors to the 
proper user-facing response as easy and invisible to the developer as possible. See the next two sections on validation 
for details.

NOTE: When an error is handled it is logged with as much information about the request as possible. It is also logged 
with a UUID that is returned to the user in both the response body and response headers, so if a customer gives you an 
error ID you can easily look up that particular error instance in your logs to get all the details of the request that 
triggered the error and what went wrong.

<a name="request_content_validation"></a>
### Incoming request content automatic validation

Your endpoints can have automatic validation done on the incoming request body before the endpoint's `execute` method 
is ever called. In order for this validation to be done the following three things must happen:

1. Your `ServerConfig.requestContentValidationService()` must return a non-null instance that performs the validation. 
By default the app wires this up to [Backstopper](https://github.com/Nike-Inc/backstopper)'s JSR 303 validation system 
for seamless translation of validation errors to user-facing errors using `SampleCoreApiError` and `ProjectApiError` 
enum values as the go-between mapping. But if you want to use a different validation system you can - just have 
`ServerConfig.requestContentValidationService()` return your own custom validation service.
2. Your endpoint must return a non-null `TypeReference` from its `requestContentType()` method. This causes Riposte to 
deserialize the raw incoming request body bytes to whatever object type you specify, which is in turn passed into the 
validation service. NOTE: This should be handled for you automatically by subclasses of `StandardEndpoint<I, O>` as 
long as you specify the `<I>` generics argument when defining your subclass. Under normal circumstances you do *not* 
need to override `requestContentType()`.
3. Your endpoint's `isValidateRequestContent(RequestInfo)` method must return true to tell Riposte that you want 
validation performed on the request body content for that endpoint. This method defaults to true so you shouldn't need 
to do anything with this method unless you want to turn validation off for that endpoint or if you only want to do 
validation sometimes depending on the `RequestInfo` passed in.

Since all the above steps are either one-time-setup or part of standard endpoint creation you shouldn't need to do 
anything under normal circumstances and you simply need to annotate your model objects with JSR 303 Bean Validation 
annotations, but knowing how it all fits together can be helpful if something isn't working as expected. 

See the `ExampleEndpoint.Post` endpoint class for a concrete example showing how all this works together. To see it in 
action start your server and send a POST to `http://localhost:8080/example` with the request body:

		{
		  "input_val_1": 1,
		  "input_val_2": "whee",
		  "throwManualError": false
		}

It should respond with a 201 and echo your request body back to you. To see the validation work, send this instead 
(note the missing "input_val_*" fields):

		{
		  "throwManualError": false
		}

You'll get back a 400 with two errors, one for each JSR 303 violation. Take a look at 
`ExampleEndpoint.ErrorHandlingEndpointArgs` and notice how each JSR 303 annotation's message is a string representing 
one of the `ProjectApiError`'s enum values. This is how the error handling system knows how to map a validation 
violation to a proper user-facing error response.

**IMPORTANT NOTE:** Since the JSR 303 annotation's `message` field must be a string, and those strings must map to a 
`SampleCoreApiError` or `ProjectApiError` enum, there is the potential for typos, copy/paste errors, or other problems 
that prevent the JSR 303 annotation's `message` from lining up properly. The `VerifyJsr303ContractTest` class is a unit 
test already set up in the template application designed to prevent these errors. It trolls through your application 
classes looking for JSR 303 annotations and makes sure that each one's message can be successfully mapped to a 
`SampleCoreApiError` or `ProjectApiError`. *Do not disable or delete this unit test!*

<a name="arbitrary_jsr303_validation"></a>
### Java Bean Validation (JSR 303) on arbitrary objects

You are not limited to the automatic request content validation for using the JSR 303 validation services. If you want 
to run the same JSR 303 validation on arbitrary objects at arbitrary times, and have any violations automatically 
kicked to the error handling system and handled the same way, you can do so. Simply `@Inject` a 
`ClientDataValidationService` or a `FailFastServersideValidationService` into your code and call one of the `validate*` 
methods.

`ClientDataValidationService` is intended for validating *user-supplied* data in a way that will map to a HTTP status 
4xx error with details given to the user on what went wrong. This is what the automatic incoming request body 
validation uses.

`FailFastServersideValidationService` is intended for validating *serverside* data in a way that will map to a HTTP 
status 5xx error with a generic message for the user, but logged in the system with the details of the specific JSR 303 
violations that occurred. You would use this (for example) to make sure you're sending downstream services valid 
objects and receiving valid objects back. Under normal circumstances no violations would occur, but *if they do* then 
we don't leak details about our server internals to the user while still logging all the relevant information so the 
developers can investigate and fix the bugs.

[back to top](#top)

<a name="metrics"></a>
## Metrics

Codahale Metrics are supported out of the box, and several useful Riposte server metrics are gathered including 
detailed throughput and latency info about each endpoint (either per-endpoint, or grouped by HTTP method, or grouped by 
HTTP response code), and several other metrics. You can also enable JVM metrics by setting the 
`metrics.reportJvmMetrics` property to true in your properties files or passing it in as a System Property on app 
launch. 

There are several reporting options you can choose from to retrieve the metrics, each enabled or disabled with a 
property from your application properties files:

* JMX Reporting - The metrics are reported via JMX by default. If you want to turn this off you can set 
`metrics.jmx.reporting.enabled` to false, however under most circumstances you can leave it on. You can use standard 
JMX tools like JConsole to retrieve or view the metrics.
* SLF4J Reporting - You can spit out the raw Codahale Metrics data to your SLF4J log file periodically by setting 
`metrics.slf4j.reporting.enabled` to true. This is spammy so it's turned off by default.
* Graphite Reporting - Graphite reporting is also possible. Make sure the `metrics.graphite.url` and 
`metrics.graphite.port` properties are set correctly and set the `metrics.graphite.reporting.enabled` property to true 
and the metrics will be reported to Graphite. This is disabled by default as not everyone uses Graphite.
* SignalFx Reporting - There is Riposte support for SignalFx if you use that. It's not shown in this template project, 
but if you'd like to use it then you'll need to do the following:
    + Pull in the `"com.nike.riposte:riposte-metrics-codahale-signalfx:$riposteVersion"` dependency.
    + Create and expose a singleton `SignalFxReporterFactory` configured the way you want it. Inject it into 
    `AppGuiceModule.metricsReporters()` and include it in the returned `List<ReporterFactory>`.
    + Inject that same `SignalFxReporterFactory` into `AppGuiceModule.metricsListener()` and configure the returned 
    `CodahaleMetricsListener` like so:
``` java
return CodahaleMetricsListener
    .newBuilder(metricsCollector)
    .withEndpointMetricsHandler(
        new SignalFxEndpointMetricsHandler(signalFxReporterFactory, 
                                           metricsCollector.getMetricRegistry())
    )
    .withServerStatsMetricNamingStrategy(
        CodahaleMetricsListener.MetricNamingStrategy.defaultNoPrefixImpl()
    )
    .withServerConfigMetricNamingStrategy(
        CodahaleMetricsListener.MetricNamingStrategy.defaultNoPrefixImpl()
    )
    .withRequestAndResponseSizeHistogramSupplier(
        () -> new Histogram(new SlidingTimeWindowReservoir(signalFxReporterFactory.getInterval(), 
                                                           signalFxReporterFactory.getTimeUnit()))
    )
    .build();
```

These options are not mutually exclusive. You can have multiple metrics reporters enabled at the same time, and you can 
add your own custom reporters - just follow the pattern in `AppGuiceModule.metricsReporters(...)`.

Riposte contains a convenience object for tracking and reporting on custom metrics in addition to the 
automatically-handled server metrics. Inject `CodahaleMetricsCollector` into your code and use `getMetricRegistry()` to 
retrieve the Codahale `MetricRegistry` and register any metrics you want. `CodahaleMetricsCollector` also contains 
convenience helper methods for timers, meters, and counters - simply pass lambdas to the `timed(...)`, `metered(...)`, 
or `counted(...)` methods to have those lambdas measured.
 
[back to top](#top)

<a name="remote_tests"></a>
## Remote tests submodule

The remote-tests submodule is intended for functional tests, performance tests, and other remote tests where you're 
making HTTP calls against a fully independent outside environment application stack. These types of tests don't make 
sense to run at compiletime and are intended to run against a specified environment so they are segregated from the 
main application's tests found in the core-code submodule. As an example, the remote-tests submodule comes with a 
functional test for verifying that basic auth is working correctly (`BasicAuthVerificationFunctionalTest`). You execute 
the functional tests with the following gradle command: `./gradlew functionalTest -DremoteTestEnv=[environment]`

The value of the `-DremoteTestEnv=[environment]` System property can be `local`, `test`, or `prod`, and is used to 
determine which of the `*-functionaltest-[environment].conf` properties files to load when running the tests. Running 
against your `local` environment should work properly as long as the application is running locally. To run against a 
deployed test or prod environment you'll need to fix the host property in the `*-functionaltest-test.conf` and 
`*-functionaltest-prod.conf` files for your deployed project.

[back to top](#top)

<a name="component_tests"></a>
## Component tests

Since Riposte servers start up quickly (usually less than 1 second) and don't require a container to run, they can be 
launched during unit tests in order to test your application from a black-box perspective. They can be thought of as 
integration or end-to-end tests, but they run at compile time along with the rest of your unit tests so you know 
immediately when something breaks rather than waiting until you've deployed your application into a test environment 
and run functional tests against it. See `VerifyExampleEndpointComponentTest` and 
`VerifyBasicAuthIsConfiguredCorrectlyComponentTest` for examples. This is a powerful technique that can give you high 
confidence that major refactors did not break your application's API contracts (or functionality in general). 

[back to top](#top)

<a name="removing_example_code"></a>
## Removing the example code

Once you're satisfied that you understand how to build endpoints and use the error handling & validation system you're 
free to delete the example stuff from the template project. Simply search for `TODO: EXAMPLE CLEANUP` in the project 
and follow the instructions in each comment to remove the example stuff from that area. The main important pieces are:
 
* Delete the `Example*Endpoint` classes.
* Remove references to the example endpoint classes in `AppGuiceModule`.
* Remove the example error enum values from `ProjectApiError`.
* Remove the Cassandra-related dependencies in the various `build.gradle` files.

There are a few other things you might want to clean up depending on your needs - again just do a search for 
`TODO: EXAMPLE CLEANUP` in the project.

[back to top](#top)

<a name="license"></a>
## License

This Riposte microservice template is released under the 
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

[travis]:https://travis-ci.org/Nike-Inc/riposte-microservice-template
[travis img]:https://api.travis-ci.org/Nike-Inc/riposte-microservice-template.svg?branch=master

[license]:LICENSE.txt
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
