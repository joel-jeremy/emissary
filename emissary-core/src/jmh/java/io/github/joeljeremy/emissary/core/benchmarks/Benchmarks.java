package io.github.joeljeremy.emissary.core.benchmarks;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Notification;
import an.awesome.pipelinr.Pipelinr;
import an.awesome.pipelinr.Voidy;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import io.github.joeljeremy.emissary.core.Emissary;
import io.github.joeljeremy.emissary.core.Event;
import io.github.joeljeremy.emissary.core.EventHandler;
import io.github.joeljeremy.emissary.core.Request;
import io.github.joeljeremy.emissary.core.RequestHandler;
import io.github.joeljeremy.emissary.core.benchmarks.Benchmarks.EmissaryEventHandler;
import io.github.joeljeremy.emissary.core.benchmarks.Benchmarks.EmissaryRequestHandler;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;

@Warmup(time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2)
public abstract class Benchmarks {
  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private EmissaryRequest emissaryRequest;
    private EmissaryRequestHandler emissaryRequestHandler;
    private Emissary emissaryRequestDispatcher;
    private EmissaryEvent emissaryEvent;
    private EmissaryEventHandler emissaryEventHandler;
    private Emissary emissaryEventPublisher;

    private SpringEvent springEvent;
    private SpringEventListener springEventListener;
    private ApplicationEventPublisher springEventPublisher;

    private PipelinrCommand pipelinrCommand;
    private PipelinrCommandHandler pipelinrCommandHandler;
    private Pipelinr commandPipelinr;
    private PipelinrNotification pipelinrNotification;
    private PipelinrNotificationHandler pipelinrNotificationHandler;
    private Pipelinr notificationPipelinr;

    private GreenRobotEventBusMessage greenRobotEventBusMessage;
    private GreenRobotEventBusSubscriber greenRobotEventBusSubscriber;
    private org.greenrobot.eventbus.EventBus greenRobotEventBus;

    private GuavaEventBusMessage guavaEventBusMessage;
    private GuavaEventBusSubscriber guavaEventBusSubscriber;
    private com.google.common.eventbus.EventBus guavaEventBus;

    private MBassadorEvent mbassadorEvent;
    private MBassadorEventHandler mbassadorEventListener;
    private MBassador mbassador;

    private RxJavaEvent rxJavaEvent;
    private RxJavaObserver rxJavaObserver;
    private RxJavaBus rxJavaBus;

    private OttoEvent ottoEvent;
    private OttoEventSubscriber ottoEventSubscriber;
    private Bus ottoBus;

    @Setup
    public void setup(Blackhole blackhole) throws Throwable {
      // Emissary.

      emissaryRequest = new EmissaryRequest();
      emissaryRequestHandler = new EmissaryRequestHandler(blackhole);
      emissaryRequestDispatcher =
          Emissary.builder()
              .instanceProvider(c -> emissaryRequestHandler)
              .requests(config -> config.handlers(EmissaryRequestHandler.class))
              .build();

      emissaryEvent = new EmissaryEvent();
      emissaryEventHandler = new EmissaryEventHandler(blackhole);
      emissaryEventPublisher =
          Emissary.builder()
              .instanceProvider(c -> emissaryEventHandler)
              .events(config -> config.handlers(EmissaryEventHandler.class))
              .build();

      // Spring

      springEvent = new SpringEvent();
      springEventListener = new SpringEventListener(blackhole);
      var context = new GenericApplicationContext();
      context.registerBean(SpringEventListener.class, () -> springEventListener);
      context.refresh();
      springEventPublisher = context;

      // Pipelinr

      pipelinrCommand = new PipelinrCommand();
      pipelinrCommandHandler = new PipelinrCommandHandler(blackhole);
      commandPipelinr = new Pipelinr().with(() -> Stream.of(pipelinrCommandHandler));

      pipelinrNotification = new PipelinrNotification();
      pipelinrNotificationHandler = new PipelinrNotificationHandler(blackhole);
      notificationPipelinr = new Pipelinr().with(() -> Stream.of(pipelinrNotificationHandler));

      // GreenRobot EventBus

      greenRobotEventBusMessage = new GreenRobotEventBusMessage();
      greenRobotEventBusSubscriber = new GreenRobotEventBusSubscriber(blackhole);
      greenRobotEventBus = org.greenrobot.eventbus.EventBus.getDefault();
      greenRobotEventBus.register(greenRobotEventBusSubscriber);

      // Guava EventBus

      guavaEventBusMessage = new GuavaEventBusMessage();
      guavaEventBusSubscriber = new GuavaEventBusSubscriber(blackhole);
      guavaEventBus = new com.google.common.eventbus.EventBus();
      guavaEventBus.register(guavaEventBusSubscriber);

      mbassadorEvent = new MBassadorEvent();
      mbassadorEventListener = new MBassadorEventHandler(blackhole);
      mbassador = new MBassador();
      mbassador.subscribe(mbassadorEventListener);

      // RxJava

      rxJavaEvent = new RxJavaEvent();
      rxJavaObserver = new RxJavaObserver(blackhole);
      rxJavaBus = new RxJavaBus();
      rxJavaBus.toObserverable().subscribe(rxJavaObserver);

      // Otto

      ottoEvent = new OttoEvent();
      ottoEventSubscriber = new OttoEventSubscriber(blackhole);
      ottoBus = new Bus(ThreadEnforcer.ANY);
      ottoBus.register(ottoEventSubscriber);
    }
  }

  /** Benchmarks that measure average time. */
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public static class BenchmarksAvgt extends Benchmarks {}

  /** Benchmarks that measure throughput. */
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public static class BenchmarksThrpt extends Benchmarks {}

  @Benchmark
  public void emissaryEvent(BenchmarkState state) {
    state.emissaryEventPublisher.publish(state.emissaryEvent);
  }

  @Benchmark
  public void springEvent(BenchmarkState state) {
    state.springEventPublisher.publishEvent(state.springEvent);
  }

  @Benchmark
  public void pipelinrNotification(BenchmarkState state) {
    state.notificationPipelinr.send(state.pipelinrNotification);
  }

  @Benchmark
  public void greenRobotEventBusEvent(BenchmarkState state) {
    state.greenRobotEventBus.post(state.greenRobotEventBusMessage);
  }

  @Benchmark
  public void guavaEventBusEvent(BenchmarkState state) {
    state.guavaEventBus.post(state.guavaEventBusMessage);
  }

  @Benchmark
  public void mbassadorEvent(BenchmarkState state) {
    state.mbassador.publish(state.mbassadorEvent);
  }

  @Benchmark
  public void rxJavaSerializedEvent(BenchmarkState state) {
    state.rxJavaBus.send(state.rxJavaEvent);
  }

  @Benchmark
  public void ottoEvent(BenchmarkState state) {
    state.ottoBus.post(state.ottoEvent);
  }

  // Single dispatch benchmarks.

  @Benchmark
  public void emissaryRequest(BenchmarkState state) {
    state.emissaryRequestDispatcher.send(state.emissaryRequest);
  }

  @Benchmark
  public void pipelinrCommand(BenchmarkState state) {
    state.commandPipelinr.send(state.pipelinrCommand);
  }

  public static class EmissaryRequest implements Request<Void> {}

  public static class EmissaryRequestHandler {
    private final Blackhole blackhole;

    public EmissaryRequestHandler(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @RequestHandler
    public void handle(EmissaryRequest request) {
      blackhole.consume(request);
    }
  }

  public static class EmissaryEvent implements Event {}

  public static class EmissaryEventHandler {
    private final Blackhole blackhole;

    public EmissaryEventHandler(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @EventHandler
    public void handle(EmissaryEvent event) {
      blackhole.consume(event);
    }
  }

  public static class SpringEvent {}

  public static class SpringEventListener {
    private final Blackhole blackhole;

    public SpringEventListener(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @EventListener
    public void handle(SpringEvent event) {
      blackhole.consume(event);
    }
  }

  public static class PipelinrCommand implements Command<Voidy> {}

  public static class PipelinrCommandHandler implements Command.Handler<PipelinrCommand, Voidy> {
    private final Blackhole blackhole;

    public PipelinrCommandHandler(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @Override
    public Voidy handle(PipelinrCommand command) {
      blackhole.consume(command);
      return null;
    }
  }

  public static class PipelinrNotification implements Notification {}

  public static class PipelinrNotificationHandler
      implements Notification.Handler<PipelinrNotification> {
    private final Blackhole blackhole;

    public PipelinrNotificationHandler(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @Override
    public void handle(PipelinrNotification notification) {
      blackhole.consume(notification);
    }
  }

  public static class GreenRobotEventBusMessage {}

  public static class GreenRobotEventBusSubscriber {
    private final Blackhole blackhole;

    public GreenRobotEventBusSubscriber(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @org.greenrobot.eventbus.Subscribe
    public void handle(GreenRobotEventBusMessage event) {
      blackhole.consume(event);
    }
  }

  public static class GuavaEventBusMessage {}

  public static class GuavaEventBusSubscriber {
    private final Blackhole blackhole;

    public GuavaEventBusSubscriber(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @com.google.common.eventbus.Subscribe
    public void handle(GuavaEventBusMessage event) {
      blackhole.consume(event);
    }
  }

  public static class MBassadorEvent {}

  @Listener(references = References.Strong)
  public static class MBassadorEventHandler {
    private final Blackhole blackhole;

    public MBassadorEventHandler(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @Handler
    public void handle(MBassadorEvent event) {
      blackhole.consume(event);
    }
  }

  public static class RxJavaEvent {}

  public static class RxJavaObserver implements Observer<Object> {
    private final Blackhole blackhole;

    public RxJavaObserver(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    public void onNext(Object event) {
      if (event instanceof RxJavaEvent) {
        blackhole.consume(event);
      }
    }

    public void onSubscribe(Disposable d) {}

    public void onError(Throwable e) {}

    public void onComplete() {}
  }

  public static class RxJavaBus {
    private final Subject<Object> bus = PublishSubject.create().toSerialized();

    public void send(Object o) {
      bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
      return bus;
    }
  }

  public static class OttoEvent {}

  public static class OttoEventSubscriber {
    private final Blackhole blackhole;

    public OttoEventSubscriber(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @com.squareup.otto.Subscribe
    public void handle(OttoEvent event) {
      blackhole.consume(event);
    }
  }
}
