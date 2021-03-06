package io.smallrye.reactive.messaging.ack;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Acknowledgment;
import io.smallrye.reactive.messaging.annotations.Merge;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class BeanWithProcessorsManipulatingMessages extends SpiedBeanHelper {

  static final String MANUAL_ACKNOWLEDGMENT_CS = "manual-acknowledgment-cs";

  static final String NO_ACKNOWLEDGMENT = "no-acknowledgment";
  static final String NO_ACKNOWLEDGMENT_CS = "no-acknowledgment-cs";

  static final String PRE_ACKNOWLEDGMENT = "pre-acknowledgment";
  static final String PRE_ACKNOWLEDGMENT_CS = "pre-acknowledgment-cs";

  static final String POST_ACKNOWLEDGMENT = "post-acknowledgment";
  static final String POST_ACKNOWLEDGMENT_CS = "post-acknowledgment-cs";

  static final String DEFAULT_ACKNOWLEDGMENT = "default-acknowledgment";
  static final String DEFAULT_ACKNOWLEDGMENT_CS = "default-acknowledgment-cs";

  @Incoming("sink")
  @Merge
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> justTheSink(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming(MANUAL_ACKNOWLEDGMENT_CS)
  @Acknowledgment(Acknowledgment.Mode.MANUAL)
  @Outgoing("sink")
  public CompletionStage<Message<String>> processorWithAck(Message<String> input) {
    processed(MANUAL_ACKNOWLEDGMENT_CS, input);
    return input.ack().thenApply(x -> Message.of(input.getPayload() + "1"));
  }

  @Outgoing(MANUAL_ACKNOWLEDGMENT_CS)
  public Publisher<Message<String>> sourceToManualAck() {
    return ReactiveStreams.of("a", "b", "c", "d", "e")
      .map(payload ->
        Message.of(payload, () -> CompletableFuture.runAsync(() -> {
          nap();
          acknowledged(MANUAL_ACKNOWLEDGMENT_CS, payload);
        }))
      ).buildRs();
  }

  @Incoming(NO_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  @Outgoing("sink")
  public Message<String> processorWithNoAck(Message<String> input) {
    processed(NO_ACKNOWLEDGMENT, input);
    return Message.of(input.getPayload() + "1");
  }

  @Outgoing(NO_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToNoAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(NO_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(NO_ACKNOWLEDGMENT_CS)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  @Outgoing("sink")
  public CompletionStage<Message<String>> processorWithNoAckCS(Message<String> input) {
    return CompletableFuture.completedFuture(input)
      .thenApply(m -> {
        processed(NO_ACKNOWLEDGMENT_CS, input);
        return Message.of(m.getPayload() + "1");
      });
  }

  @Outgoing(NO_ACKNOWLEDGMENT_CS)
  public Publisher<Message<String>> sourceToNoAckCS() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(NO_ACKNOWLEDGMENT_CS, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(PRE_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.PRE_PROCESSING)
  @Outgoing("sink")
  public Message<String> processorWithPreAck(Message<String> input) {
    processed(PRE_ACKNOWLEDGMENT, input);
    return Message.of(input.getPayload() + "1");
  }

  @Outgoing(PRE_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToPreAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(PRE_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(PRE_ACKNOWLEDGMENT_CS)
  @Acknowledgment(Acknowledgment.Mode.PRE_PROCESSING)
  @Outgoing("sink")
  public CompletionStage<Message<String>> processorWithPreAckCS(Message<String> input) {
    processed(PRE_ACKNOWLEDGMENT_CS, input);
    return CompletableFuture.completedFuture(Message.of(input.getPayload() + "1"));
  }

  @Outgoing(PRE_ACKNOWLEDGMENT_CS)
  public Publisher<Message<String>> sourceToPreAckCS() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(PRE_ACKNOWLEDGMENT_CS, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }


  @Incoming(POST_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.POST_PROCESSING)
  @Outgoing("sink")
  public Message<String> processorWithPostAck(Message<String> input) {
    processed(POST_ACKNOWLEDGMENT, input);
    return Message.of(input.getPayload() + "1");
  }

  @Outgoing(POST_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToPostAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(POST_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(POST_ACKNOWLEDGMENT_CS)
  @Acknowledgment(Acknowledgment.Mode.POST_PROCESSING)
  @Outgoing("sink")
  public CompletionStage<Message<String>> processorWithPostAckCS(Message<String> input) {
    processed(POST_ACKNOWLEDGMENT_CS, input);
    return CompletableFuture.completedFuture(Message.of(input.getPayload() + "1"));
  }

  @Outgoing(POST_ACKNOWLEDGMENT_CS)
  public Publisher<Message<String>> sourceToPostCSAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(POST_ACKNOWLEDGMENT_CS, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }


  @Incoming(DEFAULT_ACKNOWLEDGMENT)
  @Outgoing("sink")
  public Message<String> processorWithDefaultAck(Message<String> input) {
    processed(DEFAULT_ACKNOWLEDGMENT, input);
    return Message.of(input.getPayload() + "1");
  }

  @Outgoing(DEFAULT_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToDefaultAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(DEFAULT_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(DEFAULT_ACKNOWLEDGMENT_CS)
  @Outgoing("sink")
  public CompletionStage<Message<String>> processorWithDefaultAckCS(Message<String> input) {
    processed(DEFAULT_ACKNOWLEDGMENT_CS, input);
    return CompletableFuture.completedFuture(Message.of(input.getPayload() + "1"));
  }

  @Outgoing(DEFAULT_ACKNOWLEDGMENT_CS)
  public Publisher<Message<String>> sourceToDefaultAckCS() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(DEFAULT_ACKNOWLEDGMENT_CS, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

}
