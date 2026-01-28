package io.github.joeljeremy.emissary.core;

import static java.util.Objects.requireNonNull;

import io.github.joeljeremy.emissary.core.Emissary.Builder.EventHandlingConfiguration;
import io.github.joeljeremy.emissary.core.Emissary.Builder.RequestHandlingConfiguration;
import io.github.joeljeremy.emissary.core.internal.registries.EmissaryEventHandlerRegistry;
import io.github.joeljeremy.emissary.core.internal.registries.EmissaryRequestHandlerRegistry;
import io.github.joeljeremy.emissary.core.invocationstrategies.SyncEventHandlerInvocationStrategy;
import io.github.joeljeremy.emissary.core.invocationstrategies.SyncRequestHandlerInvocationStrategy;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/** Checkout Emissary! */
public class Emissary implements Dispatcher, Publisher {
  private static final Logger LOGGER = System.getLogger(Emissary.class.getName());

  private final RequestHandlerProvider requestHandlerProvider;
  private final Supplier<RequestHandlerInvocationStrategy> requestHandlerInvocationStrategyProvider;
  private final EventHandlerProvider eventHandlerProvider;
  private final Supplier<EventHandlerInvocationStrategy> eventHandlerInvocationStrategyProvider;

  /**
   * Constructor.
   *
   * @param instanceProvider The instance provider.
   * @param requestConfiguration The request configuration.
   * @param eventConfiguration The event configuration.
   */
  private Emissary(
      InstanceProvider instanceProvider,
      RequestHandlingConfiguration requestConfiguration,
      EventHandlingConfiguration eventConfiguration) {
    this.requestHandlerProvider =
        requestConfiguration.buildRequestHandlerProvider(instanceProvider);
    this.requestHandlerInvocationStrategyProvider =
        () -> requestConfiguration.getRequestHandlerInvocationStrategy(instanceProvider);
    this.eventHandlerProvider = eventConfiguration.buildEventHandlerProvider(instanceProvider);
    this.eventHandlerInvocationStrategyProvider =
        () -> eventConfiguration.getEventHandlerInvocationStrategy(instanceProvider);
  }

  /** {@inheritDoc} */
  @Override
  public <T extends Request<R>, R> Optional<R> send(T request) {
    RequestKey<T, R> requestKey = RequestKey.from(request);

    RegisteredRequestHandler<T, R> requestHandler =
        requestHandlerProvider
            .getRequestHandlerFor(requestKey)
            .orElseThrow(
                () ->
                    new EmissaryException(
                        "No request handler found for request key: " + requestKey + "."));

    RequestHandlerInvocationStrategy invocationStrategy =
        this.requestHandlerInvocationStrategyProvider.get();

    try {
      return invocationStrategy.invoke(requestHandler, request);
    } catch (Exception ex) {
      LOGGER.log(
          Level.ERROR,
          () ->
              "Exception occurred while dispatching request "
                  + request.getClass().getName()
                  + " to request handler "
                  + requestHandler
                  + ".",
          ex);

      throw ex;
    }
  }

  /** {@inheritDoc} */
  @Override
  public <T extends Event> void publish(T event) {
    @SuppressWarnings("unchecked")
    Class<T> eventType = (Class<T>) event.getClass();

    List<RegisteredEventHandler<T>> eventHandlers =
        eventHandlerProvider.getEventHandlersFor(eventType);

    EventHandlerInvocationStrategy invocationStrategy =
        this.eventHandlerInvocationStrategyProvider.get();

    try {
      invocationStrategy.invokeAll(eventHandlers, event);
    } catch (Exception ex) {
      LOGGER.log(
          Level.ERROR,
          () ->
              "Exception occurred while publishing event "
                  + event.getClass().getName()
                  + " to event handlers "
                  + eventHandlers
                  + ".",
          ex);

      throw ex;
    }
  }

  /**
   * {@link Emissary} builder.
   *
   * @return {@link Emissary} builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** The builder for {@link Emissary}. */
  public static class Builder {
    private final List<RequestHandlingConfigurator> requestConfigurators = new ArrayList<>();
    private final List<EventHandlingConfigurator> eventConfigurators = new ArrayList<>();
    private InstanceProvider instanceProvider;

    @SuppressWarnings("NullAway.Init")
    private Builder() {}

    /**
     * The instance provider to get instances from.
     *
     * @param instanceProvider The instance provider to get instances from.
     * @return Emissary builder.
     */
    public Builder instanceProvider(InstanceProvider instanceProvider) {
      this.instanceProvider = requireNonNull(instanceProvider);
      return this;
    }

    /**
     * Register a request handling configurator. Registered configurators will be executed during
     * build time in the order they were registered.
     *
     * @param requestHandlingConfigurator Register a request handling configurator. Registered
     *     configurators will be executed during build time in the order they were registered.
     * @return Emissary builder.
     */
    public Builder requests(RequestHandlingConfigurator requestHandlingConfigurator) {
      requireNonNull(requestHandlingConfigurator);
      requestConfigurators.add(requestHandlingConfigurator);
      return this;
    }

    /**
     * Register a event handling configurator. Registered configurators will be executed during
     * build time in the order they were registered.
     *
     * @param eventHandlingConfigurator Register a event handling configurator. Registered
     *     configurators will be executed during build time in the order they were registered.
     * @return Emissary builder.
     */
    public Builder events(EventHandlingConfigurator eventHandlingConfigurator) {
      requireNonNull(eventHandlingConfigurator);
      eventConfigurators.add(eventHandlingConfigurator);
      return this;
    }

    /**
     * Build {@link Emissary}.
     *
     * @return {@link Emissary}!
     */
    public Emissary build() {
      if (instanceProvider == null) {
        throw new IllegalStateException("Instance provider is required.");
      }

      var requestHandlingConfiguration = new RequestHandlingConfiguration();
      requestConfigurators.forEach(rhc -> rhc.configure(requestHandlingConfiguration));

      var eventHandlingConfiguration = new EventHandlingConfiguration();
      eventConfigurators.forEach(ehc -> ehc.configure(eventHandlingConfiguration));

      return new Emissary(
          instanceProvider, requestHandlingConfiguration, eventHandlingConfiguration);
    }

    private static <T> void requireNonNullElements(Collection<T> collection) {
      requireNonNull(collection);
      for (T element : collection) {
        requireNonNull(element);
      }
    }

    private static <T> void requireNonNullElements(T[] array) {
      requireNonNull(array);
      for (T element : array) {
        requireNonNull(element);
      }
    }

    /** Request handling configuration. */
    public static final class RequestHandlingConfiguration {
      private final Set<Class<?>> requestHandlerClasses = new HashSet<>();
      private final Set<Class<? extends Annotation>> requestHandlerAnnotations = new HashSet<>();
      private @Nullable Class<? extends RequestHandlerInvocationStrategy>
          requestHandlerInvocationStrategyClass;

      /**
       * Register supported request handler annotations. Methods annotated with any of these
       * annotations will be treated as request handlers. The {@link RequestHandler} annotation is
       * supported by default.
       *
       * @apiNote Annotations must have runtime retention policy i.e. must be annotated with
       *     {@code @Retention(RetentionPolicy.RUNTIME)}
       * @param requestHandlerAnnotations The request handler annotations to support.
       * @return Emissary request configuration.
       */
      @SafeVarargs
      public final RequestHandlingConfiguration handlerAnnotations(
          Class<? extends Annotation>... requestHandlerAnnotations) {
        requireNonNullElements(requestHandlerAnnotations);
        Collections.addAll(this.requestHandlerAnnotations, requestHandlerAnnotations);
        return this;
      }

      /**
       * Register supported request handler annotations. Methods annotated with any of these
       * annotations will be treated as request handlers. The {@link RequestHandler} annotation is
       * supported by default.
       *
       * @apiNote Annotations must have runtime retention policy i.e. must be annotated with
       *     {@code @Retention(RetentionPolicy.RUNTIME)}
       * @param requestHandlerAnnotations The request handler annotations to support.
       * @return Emissary request configuration.
       */
      public final RequestHandlingConfiguration handlerAnnotations(
          Collection<Class<? extends Annotation>> requestHandlerAnnotations) {
        requireNonNullElements(requestHandlerAnnotations);
        this.requestHandlerAnnotations.addAll(requestHandlerAnnotations);
        return this;
      }

      /**
       * Scan class for methods annotated with supported request handler annotations and register
       * them as request handlers.
       *
       * @param requestHandlerClasses The classes to scan for supported request handler annotations.
       * @return Emissary request configuration.
       */
      public final RequestHandlingConfiguration handlers(Class<?>... requestHandlerClasses) {
        requireNonNullElements(requestHandlerClasses);
        Collections.addAll(this.requestHandlerClasses, requestHandlerClasses);
        return this;
      }

      /**
       * Scan class for methods annotated with supported request handler annotations and register
       * them as request handlers.
       *
       * @param requestHandlerClasses The classes to scan for supported request handler annotations.
       * @return Emissary request configuration.
       */
      public final RequestHandlingConfiguration handlers(
          Collection<Class<?>> requestHandlerClasses) {
        requireNonNullElements(requestHandlerClasses);
        this.requestHandlerClasses.addAll(requestHandlerClasses);
        return this;
      }

      /**
       * The request handler invocation strategy to use.
       *
       * @param requestHandlerInvocationStrategyClass The request handler invocation strategy to
       *     use.
       * @return Emissary request configuration.
       */
      public final RequestHandlingConfiguration invocationStrategy(
          Class<? extends RequestHandlerInvocationStrategy> requestHandlerInvocationStrategyClass) {
        requireNonNull(requestHandlerInvocationStrategyClass);
        this.requestHandlerInvocationStrategyClass = requestHandlerInvocationStrategyClass;
        return this;
      }

      private RequestHandlerProvider buildRequestHandlerProvider(
          InstanceProvider instanceProvider) {
        var requestHandlerRegistry =
            new EmissaryRequestHandlerRegistry(instanceProvider, requestHandlerAnnotations);
        return requestHandlerRegistry.register(requestHandlerClasses.toArray(Class<?>[]::new));
      }

      private RequestHandlerInvocationStrategy getRequestHandlerInvocationStrategy(
          InstanceProvider instanceProvider) {
        try {
          if (requestHandlerInvocationStrategyClass == null
              || requestHandlerInvocationStrategyClass
                  == SyncRequestHandlerInvocationStrategy.class) {
            return SyncRequestHandlerInvocationStrategy.DEFAULT;
          }

          return (RequestHandlerInvocationStrategy)
              instanceProvider.getInstance(requestHandlerInvocationStrategyClass);
        } catch (Exception ex) {
          throw new IllegalStateException(
              "Failed to get a request handler invocation strategy from instance provider: "
                  + requestHandlerInvocationStrategyClass,
              ex);
        }
      }
    }

    /** Event handling configuration. */
    public static final class EventHandlingConfiguration {
      private final Set<Class<?>> eventHandlerClasses = new HashSet<>();
      private final Set<Class<? extends Annotation>> eventHandlerAnnotations = new HashSet<>();
      private @Nullable Class<? extends EventHandlerInvocationStrategy>
          eventHandlerInvocationStrategyClass;

      /**
       * Register supported event handler annotations. Methods annotated with these annotations will
       * be treated as event handlers. The {@link EventHandler} annotation is supported by default.
       *
       * @apiNote Annotations must have runtime retention policy i.e. must be annotated with
       *     {@code @Retention(RetentionPolicy.RUNTIME)}
       * @param eventHandlerAnnotations The event handler annotations to support. The {@link
       *     EventHandler} annotation is supported by default.
       * @return Emissary request configuration.
       */
      @SafeVarargs
      public final EventHandlingConfiguration handlerAnnotations(
          Class<? extends Annotation>... eventHandlerAnnotations) {
        requireNonNullElements(eventHandlerAnnotations);
        Collections.addAll(this.eventHandlerAnnotations, eventHandlerAnnotations);
        return this;
      }

      /**
       * Register supported event handler annotations. Methods annotated with these annotations will
       * be treated as event handlers. The {@link EventHandler} annotation is supported by default.
       *
       * @apiNote Annotations must have runtime retention policy i.e. must be annotated with
       *     {@code @Retention(RetentionPolicy.RUNTIME)}
       * @param eventHandlerAnnotations The event handler annotations to support. The {@link
       *     EventHandler} annotation is supported by default.
       * @return Emissary request configuration.
       */
      public final EventHandlingConfiguration handlerAnnotations(
          Collection<Class<? extends Annotation>> eventHandlerAnnotations) {
        requireNonNullElements(eventHandlerAnnotations);
        this.eventHandlerAnnotations.addAll(eventHandlerAnnotations);
        return this;
      }

      /**
       * Scan class for methods annotated with supported event handler annotations and register them
       * as event handlers.
       *
       * @param eventHandlerClasses The classes to scan for supported event handler annotations.
       * @return Emissary event configuration.
       */
      public final EventHandlingConfiguration handlers(Class<?>... eventHandlerClasses) {
        requireNonNullElements(eventHandlerClasses);
        Collections.addAll(this.eventHandlerClasses, eventHandlerClasses);
        return this;
      }

      /**
       * Scan class for methods annotated with supported event handler annotations and register them
       * as event handlers.
       *
       * @param eventHandlerClasses The classes to scan for supported event handler annotations.
       * @return Emissary event configuration.
       */
      public final EventHandlingConfiguration handlers(Collection<Class<?>> eventHandlerClasses) {
        requireNonNullElements(eventHandlerClasses);
        this.eventHandlerClasses.addAll(eventHandlerClasses);
        return this;
      }

      /**
       * The event handler invocation strategy to use.
       *
       * @param eventHandlerInvocationStrategyClass The event handler invocation strategy to use.
       * @return Emissary event configuration.
       */
      public final EventHandlingConfiguration invocationStrategy(
          Class<? extends EventHandlerInvocationStrategy> eventHandlerInvocationStrategyClass) {
        requireNonNull(eventHandlerInvocationStrategyClass);
        this.eventHandlerInvocationStrategyClass = eventHandlerInvocationStrategyClass;
        return this;
      }

      private EventHandlerProvider buildEventHandlerProvider(InstanceProvider instanceProvider) {
        var eventHandlerRegistry =
            new EmissaryEventHandlerRegistry(instanceProvider, eventHandlerAnnotations);
        return eventHandlerRegistry.register(eventHandlerClasses.toArray(Class<?>[]::new));
      }

      private EventHandlerInvocationStrategy getEventHandlerInvocationStrategy(
          InstanceProvider instanceProvider) {
        try {
          if (eventHandlerInvocationStrategyClass == null
              || eventHandlerInvocationStrategyClass == SyncEventHandlerInvocationStrategy.class) {
            return SyncEventHandlerInvocationStrategy.DEFAULT;
          }

          return (EventHandlerInvocationStrategy)
              instanceProvider.getInstance(eventHandlerInvocationStrategyClass);
        } catch (Exception ex) {
          throw new IllegalStateException(
              "Failed to get an event handler invocation strategy from instance provider: "
                  + eventHandlerInvocationStrategyClass,
              ex);
        }
      }
    }
  }

  /** Request handling configurator. */
  public static interface RequestHandlingConfigurator {
    /**
     * Configure request handling.
     *
     * @param config The request handling configuration.
     */
    void configure(RequestHandlingConfiguration config);
  }

  /** Event handling configurator. */
  public static interface EventHandlingConfigurator {
    /**
     * Configure event handling.
     *
     * @param config The event handling configuration.
     */
    void configure(EventHandlingConfiguration config);
  }

  /** Determines the strategy to use in executing request handlers. */
  public static interface RequestHandlerInvocationStrategy {
    /**
     * Invoke the request handler.
     *
     * @param <T> The request type.
     * @param <R> The result type.
     * @param requestHandler The registered request handler to invoke.
     * @param request The dispatched request.
     * @return The request result.
     */
    <T extends Request<R>, R> Optional<R> invoke(
        RegisteredRequestHandler<T, R> requestHandler, T request);
  }

  /** Determines the strategy to use in executing event handlers. */
  public static interface EventHandlerInvocationStrategy {
    /**
     * Invoke all the event handlers.
     *
     * @param <T> The event type.
     * @param eventHandlers The registered event handlers to invoke.
     * @param event The published event.
     */
    <T extends Event> void invokeAll(List<RegisteredEventHandler<T>> eventHandlers, T event);
  }
}
