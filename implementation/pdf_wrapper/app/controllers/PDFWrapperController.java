package controllers;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.play.WrapperController;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.PdfWrapperService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;

/**
 * This controller handles the access of the pdf wrapper services and manages the proper GUI rendering of the services.
 */
@Singleton
public class PDFWrapperController extends WrapperController {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(PDFWrapperController.class);

  /**
   * The SQL wrapper model.
   */
  private final PdfWrapperService pdfWrapperService;

  private final File root;


  /**
   * Creates a new SQLWrapperController object.
   * @param wrapperService The wrapper service to use.
   * @param rdfProvider The RDF provider to use.
   * @param generalWrapperConfig the general wrapper configuration.
   * @param pdfConfig The pdf wrapper configuration.
   */
  @Inject
  PDFWrapperController(PdfWrapperService wrapperService,
                       RDFProvider rdfProvider,
                       GeneralWrapperConfig generalWrapperConfig,
                       RootMapping pdfConfig) {
    super(generalWrapperConfig, rdfProvider, wrapperService);
    this.pdfWrapperService = wrapperService;

    root = new File(pdfConfig.getPdfRootDirectory());

  }

  /**
   * Provides a file by its relative file path (seen from the root folder specified in the configuration file).
   * @param relativePath The relative file path.
   * @return The file specified by the relative path.
   */
  public Result getFile(String relativePath) {

    String path;
    try {
      path = root.getCanonicalPath();
    } catch (IOException e) {
      throw new IllegalStateException("root.getCanonicalPath() shouldn't throw new Exception");
    }

    if (!(path.endsWith("/") || path.endsWith("\\"))) {
      path += "/";
    }

    path += relativePath;

    File targetFile = new File(path);

    try {
      if (!isInFolderOrSubfolder(targetFile, root)) {
        return internalServerError("Illegal file specified!");
      }
    } catch (IOException e) {
      return internalServerError("Error while processing request.");
    }

    FileInputStream target;
    try {
      target = new FileInputStream(path);
    } catch (FileNotFoundException e) {
      return internalServerError("File not found!");
    }

    String filename = targetFile.getName();
    String mimeType = Http.MimeTypes.BINARY;

    String dispositionValue = "attachement; filename=" + filename;
    return ok(target).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }


  /**
   * Provides the test page of the PDF Wrapper.
   * @return the test page of the PDF Wrapper.
   */
  public Result guiTest() {
    return ok(views.html.testGui.render());
  }

  /**
   * Provides the PDF Wrapper status and debug page.
   * @return the PDF Wrapper status and debug page.
   */
  public Result index() {
    return ok(views.html.index.render(pdfWrapperService, generalConfig));
  }

  private boolean isInFolderOrSubfolder(File targetFile, File root) throws IOException {
    String rootCanonical = root.getCanonicalPath();
    String targetCanonical = targetFile.getCanonicalPath();

    return targetCanonical.startsWith(rootCanonical + File.separator);
  }
}