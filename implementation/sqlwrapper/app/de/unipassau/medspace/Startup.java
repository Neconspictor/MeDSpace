package de.unipassau.medspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by David Goeth on 24.07.2017.
 */
@Singleton
public class Startup {

  private static Logger log = LoggerFactory.getLogger(Startup.class);

  @Inject
  public Startup(ClassLoader loader)  {
    log.info("Startuping...");
    //TestProcessor.main(new String[]{"TestProcessor"});
    log.info("done.");

  }
}