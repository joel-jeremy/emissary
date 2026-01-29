# Emissary (formerly Deezpatch)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/joel-jeremy/emissary/blob/main/LICENSE)
[![Gradle Build](https://github.com/joel-jeremy/emissary/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joel-jeremy/emissary/actions/workflows/gradle-build.yaml)
[![Code QL](https://github.com/joel-jeremy/emissary/actions/workflows/codeql.yaml/badge.svg)](https://github.com/joel-jeremy/emissary/actions/workflows/codeql.yaml)
[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/io.github.joel-jeremy.emissary/emissary-core/badge.svg)](https://maven-badges.sml.io/sonatype-central/io.github.joel-jeremy.emissary/emissary-core)
[![codecov](https://codecov.io/gh/joel-jeremy/emissary/graph/badge.svg?token=1KVQMOXYHT)](https://codecov.io/gh/joel-jeremy/emissary)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.emissary&metric=coverage)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=bugs)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=io.github.joel-jeremy.externalized-properties&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
[![Discord](https://img.shields.io/discord/1025648239162175578.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/bAfgXRVx3T)
<!-- Commenting out until issue gets fixed: https://github.com/snyk/cli/issues/668 -->
<!-- [![Known Vulnerabilities](https://snyk.io/test/github/joel-jeremy/emissary/badge.svg)](https://snyk.io/test/github/joel-jeremy/emissary) -->

A simple-to-use, no dependency, yet 🗲BLAZING FAST🗲 messaging library for decoupling messages (requests and events) and message handlers 🚀

Emissary aims to take advantage of the simplicity of using the annotations for handlers (e.g. `@RequestHandler`/`@EventHandler`) without the drawbacks of reflection (slow).

Emissary aims to make it easy to build applications that apply the [Command Query Responsibility Segregation](https://martinfowler.com/bliki/CQRS.html) (CQRS) pattern, but it is not in any way limited to that pattern only. 

## Like the project?

Please consider giving the repository a ⭐. It means a lot! Thank you :)

## Get Emissary

> [!IMPORTANT]  
> Up until v1.1.0, the core library is published under the old `deezpatch-core` name. This has been renamed to `emissary-core` starting from v2.0.0 onwards.

### Gradle

```groovy
implementation "io.github.joel-jeremy.emissary:emissary-core:${version}"
```

### Maven

```xml
<dependency>
    <groupId>io.github.joel-jeremy.emissary</groupId>
    <artifactId>emissary-core</artifactId>
    <version>${version}</version>
</dependency>
```

### Java 9 Module Names

> [!IMPORTANT]  
> Up until v1.1.0, the core library has the module name `io.github.joeljeremy.deezpatch.core`. This has been renamed to `io.github.joeljeremy.emissary.core` starting from v2.0.0 onwards.

Emissary jars are published with Automatic-Module-Name manifest attribute:

- Core - `io.github.joeljeremy.emissary.core`

Module authors can use above module names in their module-info.java:

```java
module foo.bar {
    requires io.github.joeljeremy.emissary.core;
}
```

## What differentiates Emissary?

### Performance

Emissary is orders of magnitude more performant than similar libraries. It takes advantage of [java.lang.invoke.LambdaMetafactory](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/LambdaMetafactory.html) to avoid the cost of invoking methods reflectively. This results in performance close to directly invoking the request handler and event handler methods!

### ~ 1000% more throughput compared to other similar libraries (Spring's ApplicationEventPublisher, Pipelinr, EventBus)
### ~ 90% faster compared to other similar libraries (Spring's ApplicationEventPublisher, Pipelinr, EventBus)

### [Java 25 Benchmarks](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/joel-jeremy/emissary/main/emissary-core/src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks/results-java25.json)

### [Java 21 Benchmarks](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/joel-jeremy/emissary/main/emissary-core/src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks/results-java21.json)

### [Java 17 Benchmarks](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/joel-jeremy/emissary/main/emissary-core/src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks/results-java17.json)

### [Java 11 Benchmarks](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/joel-jeremy/emissary/main/emissary-core/src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks/results-java11.json)

### Easy Integration with Dependency Injection (DI) Frameworks

Other libraries forces users to instantiate the handlers/subscribers during startup/creation-time (usually by manually instantiating them) e.g.

 ```java
 // Manually instantiate subscriber and all its possible dependencies
 eventBus.subscribe(new MySubscriber(new MyDependency()))
 ```
 
This means that these handlers/subscribers are not managed by your application's DI framework and are essentially singleton instances.

Emissary takes a different approach by allowing users to leverage their application's DI framework to instantiate and manage the request/event handlers. This enables the following (non-exhaustive list):

- Injection of services/dependencies
- Configurable instance lifetime/scope (singleton / new instance per message, etc.)
- Hooks to instance lifecycles (Spring's @PostConstruct and @PreDestroy, etc.)

> [!NOTE]  
> Emissary abstracts DI frameworks via an [InstanceProvider](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/InstanceProvider.java) interface. This interface can be implemented by forwarding requests to a DI framework such as Spring's `ApplicationContext`, Guice's `Injector`, etc. It can also be implemented by simply `new`-ing up the request/event handlers when not using any DI framework.
>
> Emissary will get request/event handler instances from [InstanceProvider](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/InstanceProvider.java) every time a request/event is dispatched.

#### Example with Spring Boot/ApplicationContext


```java
// Spring features work on the request/event handlers.
@Component
public class MyRequestHandler {
    private final MyDependency myDependency;

    // This works!
    public MyRequestHandler(MyDepenedency myDependency) {
        this.myDependency = myDependency;
    }

    // This also works!
    @PostConstruct
    public void postConstruct() {}

    // This works too!
    @PreDestroy
    public void preDestroy() {}

    @RequestHandler
    public void handle(MyRequest request) {
        // Handle
    }
}
```

##### Spring Boot

```java
@Bean
public Emissary emissary(ApplicationContext applicationContext) {
    return Emissary.builder()
        // Set instanceProvider to Spring's ApplicationContext::getBean function.
        .instanceProvider(applicationContext::getBean)
        .requests(MyRequestHandler.class)
        .events(...)
        .build();
}

@RestController
public class MyFooController {
    private final Emissary emissary;

    public MyFooController(Emissary emissary) {
        this.emissary = emissary;
    }

    @PostMapping("/foo")
    public void newFoo(@RequestBody Foo foo) {
        emissary.send(new CreateNewFoo(foo));
    }
}
```

##### Plain Spring ApplicationContext

```java
public static void main(String[] args) {
  ApplicationContext applicationContext = springApplicationContext();
  Emissary emissary = Emissary.builder()
        // Set instanceProvider to Spring's ApplicationContext::getBean function.
        .instanceProvider(applicationContext::getBean)
        .requests(MyRequestHandler.class)
        .events(...)
        .build();
}
```

#### Example with Guice's Injector

```java
public static void main(String[] args) {
  Injector injector = guiceInjector();
  Emissary emissary = Emissary.builder()
        // Set instanceProvider to Guice's Injector::getInstance function.
        .instanceProvider(injector::getInstance)
        .requests(...)
        .events(...)
        .build();
}
```

#### Example with No DI framework

```java
// Application.java

public static void main(String[] args) {
  Emissary emissary = Emissary.builder()
        // Set instanceProvider to own implementation
        .instanceProvider(Application::getInstance)
        .requests(...)
        .events(...)
        .build();
}

private static Object getInstance(Class<?> handlerType) {
  if (MyRequestHandler.class.equals(handlerType)) {
    return new MyRequestHandler();
  } else if (MyEventHandler.class.equals(handlerType)) {
    return new MyEventHandler();
  }

  throw new IllegalStateException("Failed to get instance for " + handlerType.getName() + ".");
}
```

## Concepts and Terminologies

### Requests

Requests are messages that either:

1. Initiate a state change/mutation
    - Commands in [CQRS](https://martinfowler.com/bliki/CQRS.html)
2. Retrieve/query data
    - Queries in [CQRS](https://martinfowler.com/bliki/CQRS.html)

```java
public class CreateFooCommand implements Request<Void> {
    private final String name;
    
    public CreateFooCommand(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }
}

public class GetFooByNameQuery implements Request<Foo> {
    private final String name;
    
    public GetFooByNameQuery(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }
}
```

### Request Handlers

Requests are handled by request handlers. Request handlers can be registered through the use of the [@RequestHandler](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/RequestHandler.java) annotation.

A request must only have a single request handler.

**(`@RequestHandler`s fully support methods with `void` return types! No need to set method return type to `Void` and return `null` for no reason.)**

```java
public class CreateFooCommandHandler {
    @RequestHandler
    public void handle(CreateFooCommand command) {
        insertFooToDatabase(command.name());
    }
}

public class GetFooQueryHandler {
    @RequestHandler
    public Foo handle(GetFooByNameQuery query) {
        return getFooFromDatabase(query.name());
    }
}
```

### Request Dispatcher

Requests are dispatched to a single request handler and this can be done through a dispatcher.

```java
public static void main(String[] args) {
    // Use Spring's application context as InstanceProvider in this example
    // but any other DI framework can be used e.g. Guice, Dagger, etc.
    ApplicationContext applicationContext = springApplicationContext();

    // Emissary implements the Dispatcher interface.
    Dispatcher dispatcher = Emissary.builder()
        .instanceProvider(applicationContext::getBean)
        .requests(config -> config.handlers(CreateFooCommandHandler.class, GetFooQueryHandler.class))
        .build();

    // Send command!
    dispatcher.send(new CreateFooCommand("Emissary"));

    // Send query!
    Optional<Pong> pong = dispatcher.send(new GetFooByNameQuery("Emissary"));
}
```

### Events

Events are messages that indicate that something has occurred in the system.

```java
public class FooCreatedEvent implements Event {
    private final String name;

    public FooCreatedEvent(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
```

### Event Handlers

Events are handled by event handlers. Event handlers can be registered through the use of the [@EventHandler](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/EventHandler.java) annotation.

An event can have zero or more event handlers.

```java
public class FooEventHandler {
    @EventHandler
    public void notifyA(FooCreatedEvent event) {
        helloFoo(event.name());
    }

    @EventHandler
    public void notifyB(FooCreatedEvent event) {
        kumustaFoo(event.name());
    }
}
```

### Event Publisher

Events are dispatched to zero or more event handlers and this can be done through a publisher.

```java
public static void main(String[] args) {
    // Use Spring's application context as InstanceProvider in this example
    // but any other DI framework can be used e.g. Guice, Dagger, etc.
    ApplicationContext applicationContext = springApplicationContext();

    // Emissary implements the Publisher interface.
    Publisher publisher = Emissary.builder()
        .instanceProvider(applicationContext::getBean)
        .events(config -> config.handlers(FooEventHandler.class))
        .build();

    // Publish event!
    publisher.publish(new FooCreatedEvent("Emissary"));
}
```

## Advanced Features

### Custom Request/Event Handler Annotations

In cases where a project is built in such a way that bringing in external dependencies is considered a bad practice (e.g. domain layer/package in a Hexagonal (Ports and Adapters) architecture), Emissary provides a way to use custom request/event handler annotations (in addition to the built-in [RequestHandler](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/RequestHandler.java) and [EventHandler](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/EventHandler.java) annotations) to annotate request/event handlers.

This way, Emissary can still be used without adding the core Emissary library as a dependency of a project's domain layer/package. Instead, it may be used in the outer layers/packages to wire things up.

```java
// Let's say below classes are declared in a project's core/domain package:

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AwesomeRequestHandler {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AwesomeEventHandler {}

public class MyRequestHandler {
  @AwesomeRequestHandler
  public void handle(TestRequest request) {
    // Handle.
  }
}

public class MyEventHandler {
  @AwesomeEventHandler
  public void handle(TestEvent event) {
    // Handle.
  }
}

// To wire things up:

public static void main(String[] args) {
  // Use Spring's application context as InstanceProvider in this example
  // but any other DI framework can be used e.g. Guice, Dagger, etc.
  ApplicationContext applicationContext = springApplicationContext();

  // Register handlers and custom annotations.
  Emissary emissary = Emissary.builder()
      .instanceProvider(applicationContext::getBean)
      .requests(config -> config.handlerAnnotations(AwesomeRequestHandler.class).handlers(MyRequestHandler.class))
      .events(config -> config.handlerAnnotations(AwesomeEventHandler.class).handlers(MyEventHandler.class))
      .build();
}
```

### Custom Invocation Strategies

The library provides [Emissary.RequestHandlerInvocationStrategy](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/Emissary.java) and [Emissary.EventHandlerInvocationStrategy](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/Emissary.java) interfaces as extension points to let users customize how request/event handler methods are invoked by the Dispatcher and Publisher.

Built-in implementations are:
- [SyncRequestHandlerInvocationStrategy](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/invocationstrategies/SyncRequestHandlerInvocationStrategy.java) (Default)
- [SyncEventHandlerInvocationStrategy](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/invocationstrategies/SyncEventHandlerInvocationStrategy.java) (Default)
- [AsyncEventHandlerInvocationStrategy](emissary-core/src/main/java/io/github/joeljeremy/emissary/core/invocationstrategies/AsyncEventHandlerInvocationStrategy.java)

Users can create a new implementation and override the defaults by:
```java
// Register custom invocation strategy.
Emissary emissary = Emissary.builder()
    .requests(config -> config.invocationStrategy(new CustomRetryOnErrorInvocationStrategy()))
    .events(config -> config.invocationStrategy(new CustomOrderGuaranteedInvocationStrategy()))
    .build();
```

---

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=io.github.joel-jeremy.emissary)
