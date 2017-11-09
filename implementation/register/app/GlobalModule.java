import com.google.inject.AbstractModule;

import de.unipassau.medspace.DataCollectorLifecycle;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.data_collector.DataCollector;
import de.unipassau.medspace.data_collector.rdf4j.RDF4J_DataCollector;
import de.unipassau.medspace.query_executor.QueryExecutorLifecycle;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import de.unipassau.medspace.register.RegisterLifecycle;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

/**
 * The GlobalModule is a configuration class that configures the play framework for the sql wrapper.
 * It's main purpose is the definition of Dependency Injection definitions.
 */
public class GlobalModule extends AbstractModule {

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
    super();
    this.environment = environment;
  }

  @Override
  protected void configure() {
    if (environment.asJava().isTest()) return;

    log.info("GlobuleModule configures dependencies...");
    bind(RDFProvider.class).to(RDF4J_RDFProvider.class).asEagerSingleton();
    bind(RegisterLifecycle.class).asEagerSingleton();
    bind(QueryExecutorLifecycle.class).asEagerSingleton();
    bind(ServiceInvoker.class).asEagerSingleton();
    bind(DataCollectorLifecycle.class).asEagerSingleton();
    bind(Repository.class).toProvider(DataCollectorLifecycle.class).asEagerSingleton();
    bind(DataCollector.class).to(RDF4J_DataCollector.class).asEagerSingleton();
    log.info("done.");
  }
}