package de.unipassau.medspace.wrapper.image_wrapper.play;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.play.ServerConfigProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.play.WrapperService;
import de.unipassau.medspace.common.play.wrapper.RegisterClient;
import de.unipassau.medspace.common.register.Datasource;

import de.unipassau.medspace.common.wrapper.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the Model of the SQL wrapper.
 */
@Singleton
public class ImageWrapperService extends WrapperService {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ImageWrapperService.class);


  private RegisterClient registerClient;

  private boolean connectToRegister;

  private Datasource wrapperDatasource;


  /**
   * Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param generalWrapperConfig The general wrapper configuration.
   * @param registerClient Used for communication with the register.
   * @param provider Used to read configurations.
   * @param serverConfig The server configuration
   * @param shutdownService the shutdown service
   * @param wrapper The wrapper.
   */
  @Inject
  public ImageWrapperService(ApplicationLifecycle lifecycle,
                             GeneralWrapperConfig generalWrapperConfig,
                             RegisterClient registerClient,
                             DdsmConfigProvider provider,
                             ServerConfig serverConfig,
                             ShutdownService shutdownService,
                             Wrapper wrapper) {

    super(generalWrapperConfig, wrapper);
    this.registerClient = registerClient;
    this.connectToRegister = generalConfig.getConnectToRegister();

      try {
        startup(provider, serverConfig);
      }catch(Throwable t) {
        log.error("Error while initializing the Image wrapper service", t);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      }

      lifecycle.addStopHook(() -> {
        log.info("Shutdown is executing...");
        if (connectToRegister) deregister();
        wrapper.close();
        return CompletableFuture.completedFuture(null);
      });
  }

  /**
   * Does startup the sql wrapper.
   * @param provider A provider used to access the wrapper and server configurations
   * @param serverConfig The server configuration
   * @throws IOException If an IO-Error occurs.
   */
  private void startup(DdsmConfigProvider provider, ServerConfig serverConfig) throws IOException {

    log.info("initializing...");

    if (!generalConfig.isIndexUsed()) {
      throw new IOException("This wrapper needs an index, but no index directory is stated "
          + "in the general wrapper configuration.");
    }

    Datasource.Builder builder = new Datasource.Builder();
    builder.setDescription(generalConfig.getDescription());
    builder.setRdfFormat(generalConfig.getOutputFormat());
    builder.setServices(generalConfig.getServices());
    builder.setUrl(serverConfig.getServerURL());

    try{
      wrapperDatasource = builder.build();
    }catch (NoValidArgumentException e) {
      throw new IOException("Couldn't create datasource object for sql wrapper", e);
    }


    //check connection to the register
    if (connectToRegister) {
      boolean registered = registerClient.register(wrapperDatasource, generalConfig.getRegisterURL());
      if (registered) {
        log.info("Successfuly registered to the Register.");
      } else {
        throw new IOException("Couldn't register to the Register!");
      }
    }

    log.info("Initialized Wrapper");
  }


  private void deregister() {
    boolean success = registerClient.deRegister(wrapperDatasource, generalConfig.getRegisterURL());

    if (success)
      log.info("Successfully deregistered from the register.");
    else
      log.error("Couldn't deregister from the register.");
  }
}