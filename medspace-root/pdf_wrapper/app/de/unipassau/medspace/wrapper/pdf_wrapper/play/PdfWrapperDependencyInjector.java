package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import de.unipassau.medspace.common.play.*;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

import javax.inject.Inject;

/**
 * A dependency injector for the PDF wrapper.
 */
public class PdfWrapperDependencyInjector extends WrapperDependencyInjector {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(PdfWrapperDependencyInjector.class);

  /**
   * Creates a new AppRoot object.
   * @param environment The environment of the play application.
   * @param configuration The Play application configuration.
   */
  @Inject
  public PdfWrapperDependencyInjector(Environment environment, Configuration configuration) {
    super(environment, configuration);
  }

  @Override
  protected void configure() {
    super.configure();
    if (environment.asJava().isTest()) return;

    log.info("configure dependencies...");

    bind(RootMapping.class)
        .toProvider(PdfWrapperConfigProvider.class).asEagerSingleton();

    bind(Wrapper.class)
        .toProvider(WrapperProvider.class).asEagerSingleton();

    bind(PdfWrapperService.class).asEagerSingleton();

    log.info("configured dependencies.");
  }
}