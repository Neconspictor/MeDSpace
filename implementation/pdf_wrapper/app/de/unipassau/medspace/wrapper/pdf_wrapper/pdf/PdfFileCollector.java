package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;


import de.unipassau.medspace.common.multimedia.MultiMediaCollector;
import de.unipassau.medspace.common.multimedia.MultiMediaContainer;
import de.unipassau.medspace.common.multimedia.MultiMediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A multimedia collector for PDf files.
 */
public class PdfFileCollector implements MultiMediaCollector {

  /**
   * The file extension of PDF files.
   */
  private final static String PDF_FILE_EXTENSION = "pdf";

  /**
   * Creates a new PdfFileCollector object.
   */
  public PdfFileCollector() {}

  @Override
  public List<MultiMediaContainer> collect(File root) {

    List<File> subFolders = getSubFolders(root);
    List<File> nonFolderFiles = getNonFolderFiles(root);
    List<MultiMediaContainer> result = collectContainers(nonFolderFiles);

    //recursive collecting...
    for (File subFolder : subFolders) {
      result.addAll(collect(subFolder));
    }

    return result;
  }

  private List<MultiMediaContainer> collectContainers(List<File> files) {
    List<MultiMediaContainer> result = new ArrayList<>();

    List<File> pdfFiles = filterByExtension(files, PDF_FILE_EXTENSION);
    for (File pdfFile : pdfFiles) {
      MultiMediaContainer container = createPdfFileContainer(pdfFile);
      result.add(container);
    }

    return result;
  }

  private MultiMediaContainer createPdfFileContainer(File pdfFileSource) {
    MultiMediaFile pdfFile = new MultiMediaFile();
    pdfFile.setSource(pdfFileSource);

    MultiMediaContainer container = new MultiMediaContainer();
    container.setData(Arrays.asList(pdfFile));

    return container;
  }

  private String getFileName(File file) {
    String fileName = file.getName();
    String extension = getFileExtension(file);
    if (!extension.equals(""))
      fileName.substring(0, fileName.length() - extension.length() - 1);
    return fileName;
  }

  private List<File> filterByExtension(List<File> files, String extension) {
    List<File> result = new ArrayList<>();
    extension = extension.toUpperCase();

    for (File file : files) {
      String fileExtension = getFileExtension(file).toUpperCase();
      if (extension.equals(fileExtension)) {
        result.add(file);
      }
    }

    return result;
  }

  private String getFileExtension(File file) {
    String fileName = file.getAbsolutePath();
    String extension = "";

    int i = fileName.lastIndexOf('.');
    int p = fileName.lastIndexOf(File.separatorChar);

    if (i > p) {
      extension = fileName.substring(i+1);
    }
    return extension;
  }

  private List<File> getSubFolders(File root) {
    List<File> result = new ArrayList<>();

    for (File file : root.listFiles()) {
      if (file.isDirectory()) {
        result.add(file);
      }
    }

    return result;
  }

  private List<File> getNonFolderFiles (File root) {
    List<File> result = new ArrayList<>();

    for (File file : root.listFiles()) {
      if (file.exists() && !file.isDirectory()) {
        result.add(file);
      }
    }

    return result;
  }
}