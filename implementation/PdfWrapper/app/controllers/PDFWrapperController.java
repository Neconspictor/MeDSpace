package controllers;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.play.WrapperController;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.ConfigProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.LogWrapperInputStream;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.PdfWrapperService;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
import de.unipassau.medspace.common.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xbill.DNS.Address;
import play.data.FormFactory;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
   * Creates a new SQLWrapperController
   * @param wrapperService TODO
   * @param rdfProvider
   */
  @Inject
  PDFWrapperController(PdfWrapperService wrapperService,
                       RDFProvider rdfProvider,
                       ConfigProvider configProvider) {
    super(configProvider.getGeneralWrapperConfig(), rdfProvider, wrapperService);
    this.pdfWrapperService = wrapperService;

    root = new File(configProvider.getPdfConfig().getPdfRootDirectory());

  }

  /**
   * TODO
   * @param relativePath
   * @return
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
    return ok(views.html.index.render(pdfWrapperService, generalConfig));
  }

  private boolean isInFolderOrSubfolder(File targetFile, File root) throws IOException {
    String rootCanonical = root.getCanonicalPath();
    String targetCanonical = targetFile.getCanonicalPath();

    return targetCanonical.startsWith(rootCanonical + File.separator);
  }
}