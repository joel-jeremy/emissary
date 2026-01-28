package io.github.joeljeremy.emissary.core.internal.registries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.emissary.core.InstanceProvider;
import io.github.joeljeremy.emissary.core.RegisteredRequestHandler;
import io.github.joeljeremy.emissary.core.Request;
import io.github.joeljeremy.emissary.core.RequestKey;
import io.github.joeljeremy.emissary.core.testfixtures.CustomRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.IntegerRequest;
import io.github.joeljeremy.emissary.core.testfixtures.TestInstanceProviders;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequest;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.CustomAnnotationRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.IncompatibleRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.InvalidRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.InvalidRequestHandlerMultipleParams;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.PrimitiveRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.TestRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.ThrowingVoidRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestRequestHandlers.VoidRequestHandler;
import io.github.joeljeremy.emissary.core.testfixtures.TestResult;
import io.github.joeljeremy.emissary.core.testfixtures.TrackableHandler;
import io.github.joeljeremy.emissary.core.testfixtures.VoidRequest;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EmissaryRequestHandlerRegistryTests {
  @Nested
  class Constructors {
    @Test
    @DisplayName("should throw when instance provider argument is null")
    void test1() {
      Set<Class<? extends Annotation>> requestHandlerAnnotations = Set.of();

      assertThrows(
          NullPointerException.class,
          () -> new EmissaryRequestHandlerRegistry(null, requestHandlerAnnotations));
    }

    @Test
    @DisplayName("should throw when request handler annotations argument is null")
    void test2() {
      InstanceProvider instanceProvider = TestInstanceProviders.of();

      assertThrows(
          NullPointerException.class,
          () -> new EmissaryRequestHandlerRegistry(instanceProvider, null));
    }
  }

  @Nested
  class RegisterMethod {
    @Test
    @DisplayName("should throw when request handler class argument is null")
    void test1() {
      EmissaryRequestHandlerRegistry requestHandlerRegistry = buildRequestHandlerRegistry();

      assertThrows(
          NullPointerException.class, () -> requestHandlerRegistry.register((Class<?>[]) null));
    }

    @Test
    @DisplayName("should detect and register methods annotated with @RequestHandler")
    void test2() {
      TestRequestHandler requestHandler = TestRequestHandlers.testRequestHandler();
      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(requestHandler);

      requestHandlerRegistry.register(requestHandler.getClass());

      RequestKey<TestRequest, TestResult> requestType = new RequestKey<>() {};
      assertTrue(requestHandlerRegistry.getRequestHandlerFor(requestType).isPresent());
    }

    @Test
    @DisplayName(
        "should detect and register methods annotated with custom request handler annotation")
    void test3() {
      CustomAnnotationRequestHandler customAnnotationRequestHandler =
          TestRequestHandlers.customAnnotationRequestHandler();
      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(
              Set.of(CustomRequestHandler.class), customAnnotationRequestHandler);

      requestHandlerRegistry.register(customAnnotationRequestHandler.getClass());

      RequestKey<TestRequest, TestResult> requestType = new RequestKey<>() {};
      assertTrue(requestHandlerRegistry.getRequestHandlerFor(requestType).isPresent());
    }

    @Test
    @DisplayName(
        "should support methods which declares primitive method return types in lieu of wrapper"
            + " types")
    void test4() {
      PrimitiveRequestHandler primitiveRequestHandler =
          TestRequestHandlers.primitiveRequestHandler();
      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(primitiveRequestHandler);

      requestHandlerRegistry.register(primitiveRequestHandler.getClass());

      RequestKey<IntegerRequest, Integer> requestType = new RequestKey<>() {};
      assertTrue(requestHandlerRegistry.getRequestHandlerFor(requestType).isPresent());
    }

    @Test
    @DisplayName("should support methods which declare (primitive) void method return types")
    void test5() {
      VoidRequestHandler voidRequestHandler = TestRequestHandlers.voidRequestHandler();
      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(voidRequestHandler);

      requestHandlerRegistry.register(voidRequestHandler.getClass());

      RequestKey<VoidRequest, Void> requestType = new RequestKey<>() {};
      assertTrue(requestHandlerRegistry.getRequestHandlerFor(requestType).isPresent());
    }

    @Test
    @DisplayName(
        "should throw when a method annotated with @RequestHandler does not have a parameter")
    void test6() {
      InvalidRequestHandler invalidRequestHandler = TestRequestHandlers.invalidRequestHandler();
      Class<?> requestHandlerClass = invalidRequestHandler.getClass();

      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(invalidRequestHandler);

      assertThrows(
          IllegalArgumentException.class,
          () -> requestHandlerRegistry.register(requestHandlerClass));
    }

    @Test
    @DisplayName(
        "should throw when a method annotated with @RequestHandler has more than one parameter")
    void test7() {
      InvalidRequestHandlerMultipleParams invalidRequestHandlerMultipleParams =
          TestRequestHandlers.invalidRequestHandlerMultipleParams();
      Class<?> requestHandlerClass = invalidRequestHandlerMultipleParams.getClass();

      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(invalidRequestHandlerMultipleParams);

      assertThrows(
          IllegalArgumentException.class,
          () -> requestHandlerRegistry.register(requestHandlerClass));
    }

    @Test
    @DisplayName(
        "should throw when there are multiple @RequestHandler methods that handle the same request")
    void test8() {
      VoidRequestHandler voidRequestHandler = TestRequestHandlers.voidRequestHandler();
      ThrowingVoidRequestHandler throwingVoidRequestHandler =
          TestRequestHandlers.throwingVoidRequestHandler(new RuntimeException("Oops!"));

      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(voidRequestHandler, throwingVoidRequestHandler);

      Class<?> requestHandlerClass1 = voidRequestHandler.getClass();
      Class<?> requestHandlerClass2 = throwingVoidRequestHandler.getClass();

      // Register a VoidRequest handler.
      requestHandlerRegistry.register(requestHandlerClass1);

      assertThrows(
          UnsupportedOperationException.class,
          // Register another VoidRequest handler.
          () -> requestHandlerRegistry.register(requestHandlerClass2));
    }

    @Test
    @DisplayName(
        "should ignore method with correct method signature but not annotated with @RequestHandler")
    void test9() {
      var requestHandler =
          new TrackableHandler() {
            @SuppressWarnings("unused")
            public int handle(IntegerRequest request) {
              track(request);
              return Integer.parseInt(request.parameter());
            }
          };

      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(requestHandler);

      requestHandlerRegistry.register(requestHandler.getClass());

      RequestKey<IntegerRequest, Integer> requestType = new RequestKey<>() {};

      assertFalse(requestHandlerRegistry.getRequestHandlerFor(requestType).isPresent());
    }

    @Test
    @DisplayName(
        "should throw when result type is not the same as the request handler method's return type")
    void test10() {
      IncompatibleRequestHandler incompatibleRequestHandler =
          TestRequestHandlers.incompatibleRequestHandler();
      Class<?> requestHandlerClass = incompatibleRequestHandler.getClass();

      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(incompatibleRequestHandler);

      assertThrows(
          UnsupportedOperationException.class,
          () -> requestHandlerRegistry.register(requestHandlerClass));
    }
  }

  @Nested
  class GetRequestHandlerForMethod {
    @Test
    @DisplayName("should throw when request type argument is null")
    void test1() {
      EmissaryRequestHandlerRegistry requestHandlerRegistry = buildRequestHandlerRegistry();

      assertThrows(
          NullPointerException.class, () -> requestHandlerRegistry.getRequestHandlerFor(null));
    }

    @Test
    @DisplayName("should return registered request handler for request type")
    void test2() {
      var requestHandler = TestRequestHandlers.primitiveRequestHandler();
      EmissaryRequestHandlerRegistry requestHandlerRegistry =
          buildRequestHandlerRegistry(requestHandler).register(requestHandler.getClass());

      RequestKey<IntegerRequest, Integer> requestType = new RequestKey<>() {};
      Optional<RegisteredRequestHandler<IntegerRequest, Integer>> resolved =
          requestHandlerRegistry.getRequestHandlerFor(requestType);

      assertNotNull(resolved);
      assertTrue(resolved.isPresent());

      // When registered request handler is invoked, it must invoke original
      // request handler instance.
      var request = new IntegerRequest("1");
      resolved.get().invoke(request);

      assertTrue(requestHandler.hasHandled(request));
    }

    @Test
    @DisplayName(
        "should return empty Optional when there is no registered request handler for request type")
    void test3() {
      EmissaryRequestHandlerRegistry requestHandlerRegistry = buildRequestHandlerRegistry();

      // No registrations...

      RequestKey<RequestWithNoHandler, Void> requestType = new RequestKey<>() {};
      Optional<RegisteredRequestHandler<RequestWithNoHandler, Void>> resolved =
          requestHandlerRegistry.getRequestHandlerFor(requestType);

      assertNotNull(resolved);
      assertTrue(resolved.isEmpty());
    }
  }

  private static EmissaryRequestHandlerRegistry buildRequestHandlerRegistry(
      Object... requestHandlers) {
    return new EmissaryRequestHandlerRegistry(TestInstanceProviders.of(requestHandlers), Set.of());
  }

  private static EmissaryRequestHandlerRegistry buildRequestHandlerRegistry(
      Set<Class<? extends Annotation>> requestHandlerAnnotations, Object... requestHandlers) {
    return new EmissaryRequestHandlerRegistry(
        TestInstanceProviders.of(requestHandlers), requestHandlerAnnotations);
  }

  static class RequestWithNoHandler implements Request<Void> {}
}
