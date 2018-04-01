
import de.unipassau.medspace.common.play.DependencyInjector;
import de.unipassau.medspace.data_collector.DataCollectorLifecycle;
import de.unipassau.medspace.data_collector.DataCollector;
import de.unipassau.medspace.data_collector.rdf4j.LocalRepositoryManager;
import de.unipassau.medspace.data_collector.rdf4j.RDF4J_DataCollector;
import de.unipassau.medspace.global.config.GlobalConfigProvider;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

/**
 * The GlobalModule is a configuration class that configures the play framework for the global MeDSpace server.
 * It's main purpose is the definition of Dependency Injection definitions.
 */
public class GlobalModule extends DependencyInjector {

  private static Logger log = LoggerFactory.getLogger(GlobalModule.class);

  /**
   * The environment of the play application.
   */
  private final Environment environment;

  /**
   * Creates a new GlobalModule.
   * @param environment The environment of the play application.
   * @param config Not used, but the play framework needs a constructor with this parameter.
   */
  public GlobalModule(Environment environment, Configuration config) {
    super(environment, config);
    this.environment = environment;

  }

  @Override
  protected void configure() {
    // very important!
    super.configure();

    if (environment.asJava().isTest()) return;

    log.info("configure dependencies...");
    bind(ConfigMapping.class).toProvider(GlobalConfigProvider.class).asEagerSingleton();
    bind(ServiceInvoker.class).asEagerSingleton();
    bind(DataCollectorLifecycle.class).asEagerSingleton();
    bind(LocalRepositoryManager.class).toProvider(DataCollectorLifecycle.class).asEagerSingleton();
    bind(DataCollector.class).to(RDF4J_DataCollector.class).asEagerSingleton();
    log.info("configured dependencies.");
  }
}