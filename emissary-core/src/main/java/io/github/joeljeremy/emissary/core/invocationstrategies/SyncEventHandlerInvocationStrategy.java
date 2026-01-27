package io.github.joeljeremy.emissary.core.invocationstrategies;

import io.github.joeljeremy.emissary.core.Emissary;
import io.github.joeljeremy.emissary.core.Emissary.EventHandlerInvocationStrategy;
import io.github.joeljeremy.emissary.core.Event;
import io.github.joeljeremy.emissary.core.RegisteredEventHandler;
import java.util.List;

/**
 * The default {@link EventHandlerInvocationStrategy} which invokes the event handlers
 * synchronously.
 */
public class SyncEventHandlerInvocationStrategy implements Emissary.EventHandlerInvocationStrategy {

  public static final SyncEventHandlerInvocationStrategy DEFAULT = Singleton.INSTANCE;

  /** {@inheritDoc} */
  @Override
  public <T extends Event> void invokeAll(List<RegisteredEventHandler<T>> eventHandlers, T event) {
    for (RegisteredEventHandler<T> eventHandler : eventHandlers) {
      eventHandler.invoke(event);
    }
  }

  private static class Singleton {
    private static final SyncEventHandlerInvocationStrategy INSTANCE =
        new SyncEventHandlerInvocationStrategy();
  }
}
