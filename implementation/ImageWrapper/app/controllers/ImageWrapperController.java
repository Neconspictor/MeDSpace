package controllers;

import de.unipassau.medspace.common.play.WrapperController;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.image_wrapper.play.ConfigProvider;
import de.unipassau.medspace.wrapper.image_wrapper.play.ImageWrapperService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This controller handles the access of the SQL wrapper services and manages the proper GUI rendering of the services.
 */
@Singleton
public class ImageWrapperController extends WrapperController {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ImageWrapperController.class);

  /**
   * The SQL wrapper model.
   */
  private final ImageWrapperService imageWrapperService;

  /**
   * Creates a new SQLWrapperController
   * @param wrapperService TODO
   * @param rdfProvider TODO
   */
  @Inject
  ImageWrapperController(ImageWrapperService wrapperService,
                         RDFProvider rdfProvider,
                         ConfigProvider configProvider) {
    super(configProvider.getGeneralWrapperConfig(), rdfProvider, wrapperService);
    this.imageWrapperService = wrapperService;
  }

  /**
   * Provides the test page of the SQL Wrapper.
   * @return
   */
  public Result guiTest() {
    return ok(views.html.testGui.render());
  }

  /**
   * Provides the SQL Wrapper status and debug page.
   */
  public Result index() {
    return ok(views.html.index.render(imageWrapperService, generalConfig));
  }
}