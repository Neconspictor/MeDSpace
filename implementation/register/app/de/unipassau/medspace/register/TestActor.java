package de.unipassau.medspace.register;

import akka.actor.AbstractActor;
import akka.actor.Props;

/**
 * Created by David Goeth on 24.09.2017.
 */
public class TestActor extends AbstractActor {

  public static Props getProps() {
    return Props.create(TestActor.class);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(ShortProcess.class, this::shortProcess)
        .match(LongProcess.class, this::longProcess)
        .build();
  }

  private void shortProcess(ShortProcess msg) {
    sender().tell(new Ok(), self());
  }

  private void longProcess(LongProcess msg) throws InterruptedException {
    Thread.sleep(10000);
    sender().tell(new Ok(), self());
  }

  public static class ShortProcess {}
  public static class LongProcess {}
  public static class Ok {}

}
