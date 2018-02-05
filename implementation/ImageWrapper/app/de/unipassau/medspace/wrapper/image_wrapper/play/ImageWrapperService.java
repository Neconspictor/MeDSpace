package de.unipassau.medspace.wrapper.image_wrapper.play;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.stream.Stream;

import de.unipassau.medspace.common.wrapper.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the Model of the SQL wrapper.
 */
@Singleton
public class ImageWrapperService {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ImageWrapperService.class);

  /**
   * The general wrapper configuration.
   */
  private GeneralWrapperConfig generalConfig;

  /**
   * The D2R wrapper.
   */
  //private D2rWrapper<?> wrapper;

  /**
   * TODO
   */
  private RegisterClient registerClient;

  /**
   * TODO
   */
  private boolean connectToRegister;

  /**
   * TODO
   */
  private Datasource wrapperDatasource;


  /**
   * Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param registerClient Used for communication with the register.
   * @param provider Used to read configurations.
   */
  @Inject
  public ImageWrapperService(ApplicationLifecycle lifecycle,
                             RegisterClient registerClient,
                             ConfigProvider provider,
                             ShutdownService shutdownService) {

    this.registerClient = registerClient;
    this.generalConfig = provider.getGeneralWrapperConfig();

    this.connectToRegister = generalConfig.getConnectToRegister();

      try {
        startup(provider);
      }catch(ConfigException.Missing | ConfigException.WrongType e) {
        log.error("Couldn't read MeDSpace mapping d2rConfig file: ", e);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      } catch(Throwable e) {

        // Catching Throwable is regarded to be a bad habit, but as we catch the Throwable only
        // for allowing the application to shutdown gracefully, it is ok to do so.
        log.error("Failed to initialize SQL Wrapper", e);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      }

      lifecycle.addStopHook(() -> {
        log.info("Shutdown is executing...");
        if (connectToRegister) deregister();
        //wrapper.close();
        return CompletableFuture.completedFuture(null);
      });
  }

  /**
   * TODO
   * @return
   */
  public Wrapper getWrapper() {
    return null;
  }

  /**
   * Performs a keyword search on the underlying datasource or on the index if one is used.
   * @param keywords The keywords to search for.
   * @return A stream of rdf triples representing the success of the keyword search.
   * @throws IOException If an IO-Error occurs.
   * @throws NoValidArgumentException If 'keywords' is null.
   */
  public Stream<Triple> search(String keywords) throws IOException, NoValidArgumentException {

    if (keywords == null) {
      throw new NoValidArgumentException("keywords mustn't be null");
    }

    StringTokenizer tokenizer = new StringTokenizer(keywords, ", ", false);
    List<String> keywordList = new ArrayList<>();

    while(tokenizer.hasMoreTokens()) {
      keywordList.add(tokenizer.nextToken());
    }

    //KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher();
    //return searcher.searchForKeywords(keywordList);
    return null;
  }

  /**
   * Does startup the sql wrapper.
   * @param provider A provider used to access the wrapper and server configurations
   * @throws IOException If an IO-Error occurs.
   */
  private void startup(ConfigProvider provider) throws IOException {

    log.info("initializing SQL Wrapper...");

    if (!generalConfig.isIndexUsed()) {
      throw new IOException("This wrapper needs an index, but no index directory is stated "
          + "in the general wrapper configuration.");
    }

    ServerConfig serverConfig = provider.getServerConfig();

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

    log.info("Initialized SQL Wrapper");
  }

  /**
   * TODO
   */
  private void deregister() {
    boolean success = registerClient.deRegister(wrapperDatasource, generalConfig.getRegisterURL());

    if (success)
      log.info("Successfully deregistered from the register.");
    else
      log.error("Couldn't deregister from the register.");
  }
}