package de.unipassau.medspace.register.util;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.Future;

/**
 * Created by David Goeth on 23.09.2017.
 */
public class ActorUtil {

  public static Object sendAndAwait(ActorRef receiver, Object msg) throws Exception {
    return sendAndAwait(receiver, msg, Duration.create(5, "seconds"));
  }

  public static Object sendAndAwait(ActorRef receiver, Object msg, FiniteDuration waitingTime) throws Exception {
    Timeout timeout = new Timeout(waitingTime);
    scala.concurrent.Future<Object> future = Patterns.ask(receiver, msg, timeout);
    Object result = null;
    result =  Await.result(future, timeout.duration());
    return result;
  }
}