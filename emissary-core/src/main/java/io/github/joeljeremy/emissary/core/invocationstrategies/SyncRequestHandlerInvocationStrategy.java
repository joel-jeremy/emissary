package io.github.joeljeremy.emissary.core.invocationstrategies;

import io.github.joeljeremy.emissary.core.Emissary;
import io.github.joeljeremy.emissary.core.Emissary.RequestHandlerInvocationStrategy;
import io.github.joeljeremy.emissary.core.RegisteredRequestHandler;
import io.github.joeljeremy.emissary.core.Request;
import java.util.Optional;

/**
 * The default {@link RequestHandlerInvocationStrategy} which invokes the request handlers
 * synchronously.
 */
public class SyncRequestHandlerInvocationStrategy
    implements Emissary.RequestHandlerInvocationStrategy {

  public static final SyncRequestHandlerInvocationStrategy DEFAULT = Singleton.INSTANCE;

  /** {@inheritDoc} */
  @Override
  public <T extends Request<R>, R> Optional<R> invoke(
      RegisteredRequestHandler<T, R> requestHandler, T request) {

    return requestHandler.invoke(request);
  }

  private static class Singleton {
    private static final SyncRequestHandlerInvocationStrategy INSTANCE =
        new SyncRequestHandlerInvocationStrategy();
  }
}
