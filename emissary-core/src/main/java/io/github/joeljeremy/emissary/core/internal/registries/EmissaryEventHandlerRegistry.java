package io.github.joeljeremy.emissary.core.internal.registries;

import static java.util.Objects.requireNonNull;

import io.github.joeljeremy.emissary.core.Event;
import io.github.joeljeremy.emissary.core.EventHandler;
import io.github.joeljeremy.emissary.core.EventHandlerProvider;
import io.github.joeljeremy.emissary.core.EventHandlerRegistry;
import io.github.joeljeremy.emissary.core.InstanceProvider;
import io.github.joeljeremy.emissary.core.RegisteredEventHandler;
import io.github.joeljeremy.emissary.core.internal.EventHandlerMethod;
import io.github.joeljeremy.emissary.core.internal.Internal;
import io.github.joeljeremy.emissary.core.internal.LambdaFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The default event handler registry. */
@Internal
public class EmissaryEventHandlerRegistry implements EventHandlerRegistry, EventHandlerProvider {

  private final RegisteredEventHandlersByEventType eventHandlersByEventType =
      new RegisteredEventHandlersByEventType();

  private ImmutableRegisteredEventHandlersByEventType immutableEventHandlersByType =
      new ImmutableRegisteredEventHandlersByEventType(eventHandlersByEventType);

  private final InstanceProvider instanceProvider;
  private final Set<Class<? extends Annotation>> eventHandlerAnnotations;

  /**
   * Constructor.
   *
   * @param instanceProvider The instance provider.
   * @param eventHandlerAnnotations The supported event handler annotations.
   */
  public EmissaryEventHandlerRegistry(
      InstanceProvider instanceProvider, Set<Class<? extends Annotation>> eventHandlerAnnotations) {
    this.instanceProvider = requireNonNull(instanceProvider);
    this.eventHandlerAnnotations = withNativeEventHandler(requireNonNull(eventHandlerAnnotations));
  }

  /** {@inheritDoc} */
  @Override
  public EmissaryEventHandlerRegistry register(Class<?>... eventHandlerClasses) {
    requireNonNull(eventHandlerClasses);

    for (Class<?> eventHandlerClass : eventHandlerClasses) {
      Method[] methods = eventHandlerClass.getMethods();
      // Register all methods marked with @EventHandler.
      for (Method method : methods) {
        if (!isEventHandler(method)) {
          continue;
        }

        validateMethodParameters(method);
        validateReturnType(method);

        // First parameter in the method is the event object.
        register(method.getParameterTypes()[0], method);
      }
    }

    return this;
  }

  /** {@inheritDoc} */
  @Override
  public <T extends Event> List<RegisteredEventHandler<T>> getEventHandlersFor(Class<T> eventType) {
    requireNonNull(eventType);

    @SuppressWarnings({"unchecked", "rawtypes"})
    List<RegisteredEventHandler<T>> eventHandlers =
        (List) immutableEventHandlersByType.get(eventType);
    return eventHandlers;
  }

  private void register(Class<?> eventType, Method eventHandlerMethod) {
    requireNonNull(eventType);
    requireNonNull(eventHandlerMethod);

    List<RegisteredEventHandler<?>> handlers = eventHandlersByEventType.get(eventType);
    handlers.add(buildEventHandler(eventHandlerMethod, instanceProvider));

    refreshImmutableEventHandlerLists();
  }

  private void refreshImmutableEventHandlerLists() {
    // Because the underlying ClassValue has changed, we need to re-initialize the optimized
    // ClassValue so that the cached values are thrown away. The next `get` would invoke the
    // `computeValue` method again to return the updated list which includes the newly registered
    // handlers.
    immutableEventHandlersByType =
        new ImmutableRegisteredEventHandlersByEventType(eventHandlersByEventType);
  }

  private boolean isEventHandler(Method method) {
    for (Annotation annotation : method.getAnnotations()) {
      if (eventHandlerAnnotations.contains(annotation.annotationType())) {
        return true;
      }
    }
    return false;
  }

  private static RegisteredEventHandler<?> buildEventHandler(
      Method eventHandlerMethod, InstanceProvider instanceProvider) {

    requireNonNull(eventHandlerMethod);

    EventHandlerMethod eventHandlerMethodLambda =
        LambdaFactory.createLambdaFunction(eventHandlerMethod, EventHandlerMethod.class);

    final Class<?> eventHandlerClass = eventHandlerMethod.getDeclaringClass();
    final String eventHandlerString = eventHandlerMethod.toGenericString();

    // Only request event handler instance when invoked instead of during registration time.
    return new RegisteredEventHandler<Event>() {
      @Override
      public void invoke(Event event) {
        eventHandlerMethodLambda.invoke(instanceProvider.getInstance(eventHandlerClass), event);
      }

      @Override
      public String toString() {
        return eventHandlerString;
      }
    };
  }

  private static void validateMethodParameters(Method method) {
    if (method.getParameterCount() != 1) {
      throw new IllegalArgumentException(
          "Methods marked with @EventHandler must accept a single parameter which is the event"
              + " object.");
    }
  }

  private static void validateReturnType(Method method) {
    if (!void.class.equals(method.getReturnType())) {
      throw new IllegalArgumentException(
          "Methods marked with @EventHandler must have a void return type.");
    }
  }

  private static Set<Class<? extends Annotation>> withNativeEventHandler(
      Set<Class<? extends Annotation>> eventHandlerAnnotations) {
    Set<Class<? extends Annotation>> merged = new HashSet<>(eventHandlerAnnotations);
    // The native @EventHandler annotation.
    merged.add(EventHandler.class);
    return Set.copyOf(merged);
  }

  private static class RegisteredEventHandlersByEventType
      extends ClassValue<List<RegisteredEventHandler<?>>> {
    @Override
    protected List<RegisteredEventHandler<?>> computeValue(Class<?> eventType) {
      return new ArrayList<>();
    }
  }

  /**
   * Decorates the base {@link RegisteredEventHandlersByEventType} to instead return immutable
   * lists.
   */
  private static class ImmutableRegisteredEventHandlersByEventType
      extends ClassValue<List<RegisteredEventHandler<?>>> {

    private final RegisteredEventHandlersByEventType base;

    public ImmutableRegisteredEventHandlersByEventType(RegisteredEventHandlersByEventType base) {
      this.base = requireNonNull(base);
    }

    @Override
    protected List<RegisteredEventHandler<?>> computeValue(Class<?> eventType) {
      // Take advantage of the performance benefits of immutable lists.
      return List.copyOf(base.get(eventType));
    }
  }
}
