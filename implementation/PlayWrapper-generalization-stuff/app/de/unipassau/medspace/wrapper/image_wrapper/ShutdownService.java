package de.unipassau.medspace.wrapper.image_wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.api.Play;
import play.api.inject.ApplicationLifecycle;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David Goeth on 22.01.2018.
 */
public class ShutdownService {

  /**
   * TODO
   */
  public static final int EXIT_ERROR = -1;

  /**
   * TODO
   */
  public static final int EXIT_SUCCESS = 0;


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ShutdownService.class);

  /**
   * TODO
   */
  private final ApplicationLifecycle lifecycle;

  /**
   * TODO
   */
  private final Provider<Application> application;

  /**
   * TODO
   * @param lifecycle
   * @param application
   */
  @Inject
  public ShutdownService(ApplicationLifecycle lifecycle, Provider<Application> application) {
    this.lifecycle = lifecycle;
    this.application = application;
  }


  /**
   * Does a graceful shutdown.
   */
  public void gracefulShutdown(int errorCode) {
    log.info("Graceful shutdown is initiated...");
    try {
      Future<?> future = lifecycle.stop();
      Await.result(future, (scala.concurrent.duration.Duration) scala.concurrent.duration.Duration.Inf());
    } catch (Throwable t) {
      log.error("Error while calling shutdown hooks", t);
    }

    log.info("Shutdown hooks have been called.");
    log.info("Exiting program...");

    //play.Application is initialized not until the Application is running
    //This is not the case while the initialization process is performed
    Option<play.api.Application> maybe = Play.maybeApplication();
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