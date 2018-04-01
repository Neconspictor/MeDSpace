package de.unipassau.medspace.wrapper.pdf_wrapper.play;

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
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the service layer of the PDF wrapper.
 */
@Singleton
public class PdfWrapperService extends WrapperService {

  private static Logger log = LoggerFactory.getLogger(PdfWrapperService.class);


  private RegisterClient registerClient;

  private boolean connectToRegister;

  private Datasource wrapperDatasource;


  /**
   * Creates a new PdfWrapperService object.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param registerClient Used for communication with the register.
   * @param generalWrapperConfig The general wrapper configuration.
   * @param serverConfig the server configuration.
   * @param wrapper The wrapper to use.
   */
  @Inject
  public PdfWrapperService(ApplicationLifecycle lifecycle,
                           RegisterClient registerClient,
                           ServerConfig serverConfig,
                           GeneralWrapperConfig generalWrapperConfig,
                           ShutdownService shutdownService,
                           Wrapper wrapper) {

    super(generalWrapperConfig, wrapper);

      try {
        startup(serverConfig, registerClient, generalWrapperConfig);
      }catch(Throwable e) {
        log.error("Error on startup: ", e);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      }

      lifecycle.addStopHook(() -> {
        log.info("Shutdown is executing...");
        if (connectToRegister) deregister();
        wrapper.close();
        log.info("...done.");
        return CompletableFuture.completedFuture(null);
      });
  }

  private void startup(ServerConfig serverConfig,
                       RegisterClient registerClient,
                       GeneralWrapperConfig generalWrapperConfig) throws IOException {

    log.info("initializing Wrapper...");

    this.registerClient = registerClient;
    this.connectToRegister = generalConfig.getConnectToRegister();

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
      throw new IOException("Couldn't create datasource object for the wrapper", e);
    }


    //check connection to the register
    if (connectToRegister) {
      boolean registered = this.registerClient.register(wrapperDatasource, generalConfig.getRegisterURL());
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