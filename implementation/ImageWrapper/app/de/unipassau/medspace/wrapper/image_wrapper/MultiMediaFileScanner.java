package de.unipassau.medspace.wrapper.image_wrapper;

import de.unipassau.medspace.wrapper.image_wrapper.ddsm.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

/**
 * TODO
 */
public class MultiMediaFileScanner {

  private static File root = new File("F:\\bachelorThesis\\DDSM\\WinSCP-Test\\cases\\cases");

  private static String IMAGE_FILE_ENDING = "LJPEG";

  public static void main(String[] args) throws IOException {
    if (!root.isDirectory()) {
      throw new IOException("root image directory doesn't exist: " + root.getAbsoluteFile());
    }

    MultiMediaCollector collector = new DDSM_ImageCollector(IMAGE_FILE_ENDING);
    List<MultiMediaContainer> imageContainers = collector.collect(root);

    System.out.println("Collected image containers:");
    for (MultiMediaContainer container : imageContainers) {
      File icsFileSource = container.getMetaData().get(0);
      String id = createID(root, icsFileSource);
      DDSM_Image leftCC = getByNameEnding(container, "LEFT_CC" + "." + IMAGE_FILE_ENDING);
      DDSM_Image leftMLO = getByNameEnding(container, "LEFT_MLO" + "." + IMAGE_FILE_ENDING);
      DDSM_Image rightCC = getByNameEnding(container, "RIGHT_CC" + "." + IMAGE_FILE_ENDING);
      DDSM_Image rightMLO = getByNameEnding(container, "RIGHT_MLO" + "." + IMAGE_FILE_ENDING);

      IcsFile icsFile = IcsFile.parse(icsFileSource,
          id,
          leftCC,
          leftMLO,
          rightCC,
          rightMLO);

      System.out.println(icsFile);
      /*for (MultiMediaFile multiMediaFile : container.getData()) {
        if (multiMediaFile.getMetaData().size() > 0) {
          File overlay = multiMediaFile.getMetaData().get(0);
          String overlayID = createID(root, overlay);
          OverlayMetaData overlayMetaData = OverlayMetaData.parse(overlay, overlayID);
          System.out.println(overlayMetaData);
          System.out.println("overlayID= " + overlayID);
        }
      }*/
    }
    System.out.println("Found " + imageContainers.size() + " results");
  }

  private static DDSM_Image getByNameEnding(MultiMediaContainer container, String ending) throws IOException {

    for (MultiMediaFile multiMediaFile: container.getData()) {
      File source = multiMediaFile.getSource();
      if (endsWith(source, ending)) {

        OverlayMetaData overlayMetaData = null;
        if (multiMediaFile.getMetaData().size() > 0) {
          File overlay = multiMediaFile.getMetaData().get(0);
          String overlayID = createID(root, overlay);
          overlayMetaData = OverlayMetaData.parse(overlay, overlayID);
        }

        String id = createID(root, source);

        return new DDSM_Image(source, overlayMetaData, id);
      }
    }

    throw new IOException("Couldn't create DDSM_Image by name ending search: '" + ending + "'");
  }

  private static boolean endsWith(File source, String ending) {
    String name = source.getName();
    return name.matches(".*" + ending);
  }

  public static String toHexadecimal(String text, String enc) throws UnsupportedEncodingException {
    byte[] myBytes = text.getBytes(enc);
    return DatatypeConverter.printHexBinary(myBytes);
  }

  private static String createID(File root, File destination) throws UnsupportedEncodingException {
    String id = Util.createRelativePath(root, destination);
    return URLEncoder.encode(id, "UTF-8");
  }
}