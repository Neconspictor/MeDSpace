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
 * The AppRoot is a configuration class that configures the play framework for the PDF wrapper.
 * It's main purpose is the definition of Dependency Injection definitions.
 */
public class PdfWrapperDependencyInjector extends MeDSpaceDependencyInjector {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(PdfWrapperDependencyInjector.class);

  /**
   * Creates a new AppRoot object.
   * @param environment The environment of the play application.
   */
  @Inject
  public PdfWrapperDependencyInjector(Environment environment, Configuration configuration) {
    super(environment);
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