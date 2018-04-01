package de.unipassau.medspace.wrapper.sqlwrapper;

import com.google.inject.TypeLiteral;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.play.MeDSpaceDependencyInjector;
import de.unipassau.medspace.d2r.D2rWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

import java.util.Locale;

/**
 * The SqlWrapperDepdendencyInjector injects dependencies for the SQL wrapper module.
 */
public class SqlWrapperDepdendencyInjector extends MeDSpaceDependencyInjector {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(SqlWrapperDepdendencyInjector.class);

  /**
   * The environment of the play application.
   */
  private final Environment environment;

  /**
   * Creates a new SqlWrapperDepdendencyInjector object.
   * @param environment The environment of the play application.
   * @param config Not used, but the play framework needs a constructor with this parameter.
   */
  public SqlWrapperDepdendencyInjector(Environment environment, Configuration config) {
    super(environment, config);
    Locale.setDefault(Locale.US);
    this.environment = environment;
  }

  @Override
  protected void configure() {

    // very important!
    super.configure();

    if (environment.asJava().isTest()) return;

    log.info("GlobuleModule configures dependencies...");

    bind(de.unipassau.medspace.d2r.config.Configuration.class)
        .toProvider(ConfigProvider.class)
        .asEagerSingleton();

    bind(ConnectionPool.class)
        .toProvider(ConnectionPoolProvider.class)
        .asEagerSingleton();

    //Generics have to be included in a TypeLiteral
    bind(new TypeLiteral<D2rWrapper<?>>(){})
        .toProvider(WrapperProvider.class).asEagerSingleton();

    bind(SQLWrapperService.class).asEagerSingleton();

    log.info("done.");
  }
}