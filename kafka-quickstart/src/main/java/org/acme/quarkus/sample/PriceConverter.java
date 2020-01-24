package org.acme.quarkus.sample;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bean consuming data from the "prices" Kafka topic and applying some conversion.
 * The result is pushed to the "my-data-stream" stream which is an in-memory stream.
 */
@ApplicationScoped
public class PriceConverter {

  private static final double CONVERSION_RATE = 0.88;

  private static final Logger LOG = LoggerFactory.getLogger(PriceConverter.class);


  ExecutorService executor = Executors.newFixedThreadPool(5);


  //  @Incoming("prices")
  //  public Subscriber<Integer> amIAsynchronous() {
  //    return ReactiveStreams.<Integer>builder()
  //        .forEach((a) -> CompletableFuture.delayedExecutor()).build();
  //  }



    /*
    To process message asynchronously, we cannot commit the offset after the method has been executed
    since the POST_PROCESSING_ACK will kickin
    Then we have to build a chain of acknowledgement that will be acked as time flies.
     */

  private Random random = new Random();

//  @Incoming("prices")
//  @Outgoing("my-data-stream")
  public CompletionStage<Double> produceAsynchronously(Integer messageCounter) {
    int counter = messageCounter;
    System.out.printf("producing asynchronously for %d%n", counter);
    CompletableFuture<Double> result = CompletableFuture
        .supplyAsync(() -> log((double)counter),
            CompletableFuture.delayedExecutor(random.nextInt(5), TimeUnit.SECONDS));
    return result;

    //    return CompletableFuture.completedFuture((double)counter);
  }

//  @Incoming("prices")
  public CompletionStage<Double> consumeAsynchronously(Integer counter) {
    System.out.printf("consuming asynchronously for %d%n", counter);
    CompletableFuture<Double> result = CompletableFuture
        .supplyAsync(() -> log((double)counter),
            CompletableFuture.delayedExecutor(random.nextInt(5), TimeUnit.SECONDS));
    return result;
  }

  @Incoming("prices")
  @Acknowledgment(Acknowledgment.Strategy.MANUAL)
  public CompletionStage<Double> consumeAsynchronouslyWithoutAcking(Message<Integer> message) {
    int counter = message.getPayload();
    LOG.info("Consuming counter : {}", counter);
    CompletableFuture<Double> result = CompletableFuture
        .supplyAsync(() -> log((double) counter),
            CompletableFuture.delayedExecutor(random.nextInt(5), TimeUnit.SECONDS, executor));
    return result;
  }


  public static <T> T log(T obj) {
    LOG.info("Completing result for {}", obj);
    return obj;
  }

  //    @Incoming("prices")
  //    @Outgoing("my-data-stream")
  //    @Broadcast
  //    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
  //    public Publisher<Message<Double>> process(Message<Integer> message) {
  //
  //        int priceInUsd = message.getPayload();
  //        CompletableFuture<Message<Double>> result = new CompletableFuture<>();
  //
  //        Random random = new Random();
  //        int test = random.nextInt(10);
  //        if(test >= 5) {
  //            message.ack();
  //            System.out.println("acknownledged : " + priceInUsd);
  //            result.complete(Message.of((double) priceInUsd));
  //        }
  //        else {
  //            System.out.println("skipped : " + priceInUsd);
  //            CompletableFuture.supplyAsync(() -> result.complete(Message.of(-1d)),
  //                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS));
  //        }
  //
  //
  ////        if (priceInUsd % 2 == 0) {
  ////            CompletableFuture.supplyAsync(() -> result.complete((double)priceInUsd),
  ////                CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS));
  ////        } else {
  ////            result.complete((double)priceInUsd);
  ////        }
  //        return ReactiveStreams.fromCompletionStage(result).buildRs();
  //    }

}
