package io.github.joeljeremy.emissary.core.internal.registries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.emissary.core.Event;
import io.github.joeljeremy.emissary.core.EventHandler;
import io.github.joeljeremy.emissary.core.InstanceProvider;
import io.github.joeljeremy.emissary.core.RegisteredEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.CustomEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestEvent;
import io.github.joeljeremy.emissary.core.testfixtures.TestEventHandlers;
import io.github.joeljeremy.emissary.core.testfixtures.TestEventHandlers.CustomAnnotationEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestEventHandlers.InvalidEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestEventHandlers.InvalidReturnTypeEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestEventHandlers.TestEventHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestInstanceProviders;
import io.github.joeljeremy.emissary.core.testfixtures.TrackableHandler;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EmissaryEventHandlerRegistryTests {
  @Nested
  class Constructors {
    @Test
    @DisplayName("should throw when instance provider argument is null")
    void test1() {
      Set<Class<? extends Annotation>> eventHandlerAnnotations = Set.of();

      assertThrows(
          NullPointerException.class,
          () -> new EmissaryEventHandlerRegistry(null, eventHandlerAnnotations));
    }

    @Test
    @DisplayName("should throw when event handler annotations argument is null")
    void test2() {
      InstanceProvider instanceProvider = TestInstanceProviders.of();

      assertThrows(
          NullPointerException.class,
          () -> new EmissaryEventHandlerRegistry(instanceProvider, null));
    }
  }

  @Nested
  class RegisterMethod {
    @Test
    @DisplayName("should throw when event handler class argument is null")
    void test1() {
      EmissaryEventHandlerRegistry eventHandlerRegistry = buildEventHandlerRegistry();

      assertThrows(
          NullPointerException.class, () -> eventHandlerRegistry.register((Class<?>[]) null));
    }

    @Test
    @DisplayName("should detect and register methods annotated with @EventHandler")
    void test2() {
      TestEventHandler eventHandler = TestEventHandlers.testEventHandler();
      EmissaryEventHandlerRegistry eventHandlerRegistry = buildEventHandlerRegistry(eventHandler);

      eventHandlerRegistry.register(eventHandler.getClass());

      List<RegisteredEventHandler<TestEvent>> eventHandlers =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      assertNotNull(eventHandlers);

      int numberOfEventHandlers =
          (int)
              Arrays.stream(eventHandler.getClass().getMethods())
                  .filter(m -> m.isAnnotationPresent(EventHandler.class))
                  .count();

      assertEquals(numberOfEventHandlers, eventHandlers.size());
    }

    @Test
    @DisplayName(
        "should detect and register methods annotated with custom event handler annotation")
    void test3() {
      Class<CustomEventHandler> customEventHandlerAnnotation = CustomEventHandler.class;
      CustomAnnotationEventHandler customAnnotationEventHandler =
          TestEventHandlers.customAnnotationEventHandler();

      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(
              Set.of(customEventHandlerAnnotation), customAnnotationEventHandler);

      eventHandlerRegistry.register(customAnnotationEventHandler.getClass());

      List<RegisteredEventHandler<TestEvent>> eventHandlers =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      assertNotNull(eventHandlers);

      int numberOfEventHandlers =
          (int)
              Arrays.stream(customAnnotationEventHandler.getClass().getMethods())
                  .filter(m -> m.isAnnotationPresent(customEventHandlerAnnotation))
                  .count();

      assertEquals(numberOfEventHandlers, eventHandlers.size());
    }

    @Test
    @DisplayName(
        "should throw when a method annotated with @EventHandler does not have a parameter")
    void test4() {
      InvalidEventHandler invalidEventHandler = TestEventHandlers.invalidEventHandler();
      Class<?> eventHandlerClass = invalidEventHandler.getClass();

      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(invalidEventHandler);

      assertThrows(
          IllegalArgumentException.class, () -> eventHandlerRegistry.register(eventHandlerClass));
    }

    @Test
    @DisplayName(
        "should ignore method with correct method signature but not annotated with @EventHandler")
    void test5() {
      var eventHandler =
          new TrackableHandler() {
            @SuppressWarnings("unused")
            public void handle(TestEvent event) {
              track(event);
            }
          };

      EmissaryEventHandlerRegistry eventHandlerRegistry = buildEventHandlerRegistry(eventHandler);

      eventHandlerRegistry.register(eventHandler.getClass());

      assertTrue(eventHandlerRegistry.getEventHandlersFor(TestEvent.class).isEmpty());
    }

    @Test
    @DisplayName("should throw when a method annotated with @EventHandler does not return void")
    void test6() {
      InvalidReturnTypeEventHandler invalidReturnTypeEventHandler =
          TestEventHandlers.invalidReturnTypeEventHandler();
      Class<?> eventHandlerClass = invalidReturnTypeEventHandler.getClass();

      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(invalidReturnTypeEventHandler);

      assertThrows(
          IllegalArgumentException.class, () -> eventHandlerRegistry.register(eventHandlerClass));
    }
  }

  @Nested
  class GetEventHandlersForMethod {
    @Test
    @DisplayName("should throw when event type argument is null")
    void test1() {
      TestEventHandler eventHandler = TestEventHandlers.testEventHandler();
      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(eventHandler).register(eventHandler.getClass());

      assertThrows(
          NullPointerException.class, () -> eventHandlerRegistry.getEventHandlersFor(null));
    }

    @Test
    @DisplayName("should return registered event handler for event type")
    void test2() {
      TestEventHandler eventHandler = TestEventHandlers.testEventHandler();
      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(eventHandler).register(eventHandler.getClass());

      List<RegisteredEventHandler<TestEvent>> resolved =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      assertNotNull(resolved);
      assertFalse(resolved.isEmpty());

      var testEvent = new TestEvent("Test");
      resolved.forEach(h -> h.invoke(testEvent));

      assertTrue(eventHandler.hasHandled(testEvent));
    }

    @Test
    @DisplayName(
        "should return empty list when there is no registered event handler for event type")
    void test3() {
      EmissaryEventHandlerRegistry eventHandlerRegistry = buildEventHandlerRegistry();

      // No registrations...

      List<RegisteredEventHandler<EventWithNoHandlers>> resolved =
          eventHandlerRegistry.getEventHandlersFor(EventWithNoHandlers.class);

      assertNotNull(resolved);
      assertTrue(resolved.isEmpty());
    }

    @Test
    @DisplayName("should return immutable lists")
    void test4() {
      TestEventHandler eventHandler = TestEventHandlers.testEventHandler();
      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(eventHandler).register(eventHandler.getClass());

      // No registrations...
      EmissaryEventHandlerRegistry emptyEventHandlerRegistry = buildEventHandlerRegistry();

      List<RegisteredEventHandler<TestEvent>> resolved =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      List<RegisteredEventHandler<EventWithNoHandlers>> resolvedEmpty =
          emptyEventHandlerRegistry.getEventHandlersFor(EventWithNoHandlers.class);

      assertNotNull(resolved);
      assertFalse(resolved.isEmpty());
      assertThrows(UnsupportedOperationException.class, () -> resolved.add(null));

      assertNotNull(resolvedEmpty);
      assertTrue(resolvedEmpty.isEmpty());
      assertThrows(UnsupportedOperationException.class, () -> resolvedEmpty.add(null));
    }

    @Test
    @DisplayName("should return newly registered handlers in subsequent calls")
    void test5() {
      TestEventHandler eventHandler1 = TestEventHandlers.testEventHandler();
      CustomAnnotationEventHandler eventHandler2 = TestEventHandlers.customAnnotationEventHandler();
      EmissaryEventHandlerRegistry eventHandlerRegistry =
          buildEventHandlerRegistry(Set.of(CustomEventHandler.class), eventHandler1, eventHandler2)
              .register(eventHandler1.getClass());

      List<RegisteredEventHandler<TestEvent>> resolved =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      assertNotNull(resolved);
      assertFalse(resolved.isEmpty());

      var testEvent = new TestEvent("Test");
      resolved.forEach(h -> h.invoke(testEvent));

      assertTrue(eventHandler1.hasHandled(testEvent));

      // Register another handler after getEventHandlersFor calls were already made.
      eventHandlerRegistry.register(eventHandler2.getClass());

      List<RegisteredEventHandler<TestEvent>> resolvedAfterNewRegistration =
          eventHandlerRegistry.getEventHandlersFor(TestEvent.class);

      assertNotNull(resolvedAfterNewRegistration);
      assertFalse(resolvedAfterNewRegistration.isEmpty());

      var anotherTestEvent = new TestEvent("Another Test");
      resolvedAfterNewRegistration.forEach(h -> h.invoke(anotherTestEvent));

      // Event handler 2 was registered later, so it would not have handled the first event.
      assertFalse(eventHandler2.hasHandled(testEvent));
      assertTrue(eventHandler2.hasHandled(anotherTestEvent));
    }
  }

  private EmissaryEventHandlerRegistry buildEventHandlerRegistry(Object... eventHandlers) {
    return new EmissaryEventHandlerRegistry(TestInstanceProviders.of(eventHandlers), Set.of());
  }

  private EmissaryEventHandlerRegistry buildEventHandlerRegistry(
      Set<Class<? extends Annotation>> eventHandlerAnnotations, Object... eventHandlers) {
    return new EmissaryEventHandlerRegistry(
        TestInstanceProviders.of(eventHandlers), eventHandlerAnnotations);
  }

  public static class EventWithNoHandlers implements Event {}
}
