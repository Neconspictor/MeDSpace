import com.google.inject.AbstractModule;
import de.unipassau.medspace.Startup;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.D2rWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

/**
 * Created by David Goeth on 24.07.2017.
 */
public class GlobalModule extends AbstractModule {

  private static Logger log = LoggerFactory.getLogger(GlobalModule.class);

  private final Environment environment;
  private final Configuration configuration;

  public GlobalModule(Environment environment, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  public void foo() {
    D2rProxy proxy = null;
    D2rWrapper wrapper = null;
  }

  @Override
  protected void configure() {
    if (environment.asJava().isTest()) return;
    log.info("GlobuleModule configures dependencies...");
    bind(ClassLoader.class).toInstance(environment.classLoader());
    bind(Startup.class).asEagerSingleton();
    bind(MyApplicationLifeCycle.class).asEagerSingleton();
    log.info("done.");

    //throw new IllegalStateException("Couldn't initialize");
  }
}
