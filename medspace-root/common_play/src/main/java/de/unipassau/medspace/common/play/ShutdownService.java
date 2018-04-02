package de.unipassau.medspace.common.play;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;
import scala.concurrent.Await;
import scala.concurrent.Future;

import javax.inject.Inject;

/**
 * A service for gracefully shutting down a Play application.
 */
public class ShutdownService {

  /**
   * Exit code when an error had been occurred.
   */
  public static final int EXIT_ERROR = -1;

  /**
   * Exit code when no error had been occurred.
   */
  public static final int EXIT_SUCCESS = 0;


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ShutdownService.class);


  private final ApplicationLifecycle lifecycle;

  /**
   * Creates a new ShutdownService object.
   * @param lifecycle The play application lifecycle.
   */
  @Inject
  public ShutdownService(ApplicationLifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }


  /**
   * Does a graceful shutdown.
   * @param errorCode The error code to use for exiting the application.
   */
  public void gracefulShutdown(int errorCode) {
    log.info("Graceful shutdown is initiated...");
    try {
      Future<?> future = lifecycle.asScala().stop();
      Await.result(future, (scala.concurrent.duration.Duration) scala.concurrent.duration.Duration.Inf());
    } catch (Throwable t) {
      log.error("Error while calling shutdown hooks", t);
    }

    log.info("Shutdown hooks have been called.");
    log.info("Exiting program...");

    //play.Application is initialized not until the Application is running
    //This is not the case while the initialization process is performed
    //Option<play.api.Application> maybe = Play.maybeApplication();
    //if (!maybe.isEmpty()) Play.stop(maybe.get());

    // Stopping the application lifecycle is enough to trigger a graceful shutdown
    // for all modules already registered.
    // But the play framework will proceed to initialize all bind classes, that are not
    // initialized, yet.
    // The play framework has no open resources.
    // Thus, a call of System.exit is here needed (and justified).
    System.exit(errorCode);
  }
}