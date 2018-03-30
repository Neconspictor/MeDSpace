package de.unipassau.medspace.wrapper.image_wrapper.ddsm;


import de.unipassau.medspace.common.multimedia.MultiMediaCollector;
import de.unipassau.medspace.common.multimedia.MultiMediaContainer;
import de.unipassau.medspace.common.multimedia.MultiMediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A multimedia collector for DDSM image files.
 */
public class DDSM_ImageCollector implements MultiMediaCollector {

  private final static String LEFT_CC = "LEFT_CC";

  private final static String LEFT_MLO = "LEFT_MLO";

  private final static String RIGHT_CC = "RIGHT_CC";

  private final static String RIGHT_MLO = "RIGHT_MLO";

  private final static String OVERLAY = "OVERLAY";


  final String imageFileExtension;

  /**
   * Creates a new DDSM_ImageCollector object.
   * @param imageFileExtension The file extension of the images.
   */
  public DDSM_ImageCollector(String imageFileExtension) {
    this.imageFileExtension = imageFileExtension;
  }

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

    List<File> icsFiles = filterByExtension(files, "ics");
    for (File icsFile : icsFiles) {
      MultiMediaContainer container = createIcsContainer(icsFile, files);
      if (container != null)
        result.add(container);
    }

    return result;
  }

  private MultiMediaContainer createIcsContainer(File icsFile, List<File> folderFiles) {

    File leftccFile = getByNameEnding(folderFiles, "." + LEFT_CC + "." + imageFileExtension);
    File leftMloFile = getByNameEnding(folderFiles, "." + LEFT_MLO + "." + imageFileExtension);
    File rightccFile = getByNameEnding(folderFiles, "." + RIGHT_CC + "." + imageFileExtension);
    File rightMloFile = getByNameEnding(folderFiles, "." + RIGHT_MLO + "." + imageFileExtension);

    //optional files
    File leftccOverlayFile = getByNameEnding(folderFiles, "." + LEFT_CC + "." + OVERLAY);
    File leftMloOverlayFile = getByNameEnding(folderFiles, "." + LEFT_MLO + "." + OVERLAY);
    File rightccOverlayFile = getByNameEnding(folderFiles, "." + RIGHT_CC + "." + OVERLAY);
    File rightMloOverlayFile = getByNameEnding(folderFiles, "." + RIGHT_MLO + "." + OVERLAY);


    MultiMediaFile leftccImage = createImage(leftccFile, leftccOverlayFile);
    MultiMediaFile leftMloImage = createImage(leftMloFile, leftMloOverlayFile);
    MultiMediaFile rightccImage = createImage(rightccFile, rightccOverlayFile);
    MultiMediaFile rightMloImage = createImage(rightMloFile, rightMloOverlayFile);


    if (leftccImage == null
        || leftMloImage == null
        || rightccImage == null
        || rightMloImage == null) {
      return null;
    }

    MultiMediaContainer container = new MultiMediaContainer();
    container.setData(Arrays.asList(leftccImage, leftMloImage, rightccImage, rightMloImage));
    container.setMetaData(Arrays.asList(icsFile));

    return container;
  }

  private MultiMediaFile createImage(File imageFile, File overlayFile) {
    if (imageFile == null) return null;

    MultiMediaFile image = new MultiMediaFile();
    image.setSource(imageFile);

    if (overlayFile != null) {
      image.setMetaData(Arrays.asList(overlayFile));
    }

    return image;
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

  private File getByNameEnding(List<File> files, String subName) {
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.length() < subName.length()) {
        continue;
      }

      String subFileName = fileName.substring(fileName.length() - subName.length(), fileName.length());

      if (subFileName.equals(subName)) {
        return file;
      }
    }

    return null;
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