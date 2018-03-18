package controllers;

import de.unipassau.medspace.common.play.WrapperController;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.zip.ZipProject;
import de.unipassau.medspace.wrapper.image_wrapper.play.DdsmConfigProvider;
import de.unipassau.medspace.wrapper.image_wrapper.play.ImageWrapperService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

  private final File root;

  /**
   * Creates a new SQLWrapperController
   * @param wrapperService TODO
   * @param rdfProvider TODO
   */
  @Inject
  ImageWrapperController(ImageWrapperService wrapperService,
                         RDFProvider rdfProvider,
                         DdsmConfigProvider configProvider) {
    super(configProvider.getGeneralWrapperConfig(), rdfProvider, wrapperService);
    this.imageWrapperService = wrapperService;
    root = new File("_work/medspace/caseFiles/");
  }

  /**
   * Provides the test page of the SQL Wrapper.
   * @return
   */
  public Result guiTest() {
    return ok(views.html.testGui.render());
  }

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

    String filename = getFilename(path);
    String mimeType = Http.MimeTypes.BINARY;

    String dispositionValue = "attachement; filename=" + filename;
    return ok(target).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }

  public Result getDirectory(String relativePath) {
    Result result;
    try {
      result = getZipFromDirectory(relativePath);
    } catch (IOException e) {
      return internalServerError("Couldn't zip folder!");
    }

    return result;
  }


  Result getZipFromDirectory(String relativePath) throws IOException {
    String path = root.getCanonicalPath();

    if (!(path.endsWith("/") || path.endsWith("\\"))) {
      path += "/";
    }

    path += relativePath;

    if (!(path.endsWith("/") || path.endsWith("\\"))) {
      path += "/";
    }

    File targetDirectory = new File(path);

    if (!isInFolderOrSubfolder(targetDirectory, root)) {
      throw new IOException("Illegal file specified!");
    }

    if (!targetDirectory.isDirectory()) {
      throw new IOException("Directory not found!");
    }

    String directoryName = targetDirectory.getName();
    String zipFilePath = path + directoryName + ".zip";

    File zipFile = new File(zipFilePath);

    if (!zipFile.exists()) {
      ZipProject zipProject = new ZipProject(path, zipFilePath);
      zipProject.zip();
    }

    FileInputStream target = new FileInputStream(zipFile);
    String mimeType = Http.MimeTypes.BINARY;

    String dispositionValue = "attachement; filename=" + directoryName + ".zip";
    return ok(target).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }

  private boolean isInFolderOrSubfolder(File targetFile, File root) throws IOException {
    String rootCanonical = root.getCanonicalPath();
    String targetCanonical = targetFile.getCanonicalPath();

    return targetCanonical.startsWith(rootCanonical + File.separator);
  }

  private String getFilename(String path) {
    int slashIndex = path.lastIndexOf("/");
    int backSlashIndex = path.lastIndexOf("\\");

    int index = Integer.max(slashIndex, backSlashIndex);

    if (index >= 0) {
      path = path.substring(index+1, path.length());
    }
    return path;
  }

  /**
   * Provides the SQL Wrapper status and debug page.
   */
  public Result index() {
    return ok(views.html.index.render(imageWrapperService, generalConfig));
  }
}