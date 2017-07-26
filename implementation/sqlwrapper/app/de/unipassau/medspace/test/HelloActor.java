package de.unipassau.medspace.test;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

import de.unipassau.medspace.test.HelloActorProtocol.*;


public class HelloActor extends UntypedAbstractActor {

  public static Props getProps() {
    return Props.create(HelloActor.class);
  }

  public void onReceive(Object msg) throws Exception {
    if (msg instanceof SayHello) {
      Thread.sleep(1800);
      sender().tell("Hello, " + ((SayHello) msg).name, self());
    }
  }
}