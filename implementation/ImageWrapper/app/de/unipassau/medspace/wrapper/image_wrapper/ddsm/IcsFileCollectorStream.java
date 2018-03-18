package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.wrapper.image_wrapper.MultiMediaCollector;
import de.unipassau.medspace.wrapper.image_wrapper.MultiMediaContainer;
import de.unipassau.medspace.wrapper.image_wrapper.MultiMediaFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * TODO
 */
public class IcsFileCollectorStream implements Stream<IcsFile> {

  /**
   * TODO
   */
  private final File root;

  /**
   * TODO
   */
  private final List<MultiMediaContainer> imageContainers;

  /**
   * TODO
   */
  private final String imageFileEnding;

  /**
   * TODO
   * @param root
   * @param imageFileEnding
   * @throws IOException
   */
  public IcsFileCollectorStream(File root, String imageFileEnding) throws IOException {

    if (!root.isDirectory()) {
      throw new IOException("root image directory doesn't exist: " + root.getAbsoluteFile());
    }

    this.root = root;
    this.imageFileEnding = imageFileEnding;

    MultiMediaCollector collector = new DDSM_ImageCollector(imageFileEnding);
    imageContainers = collector.collect(root);
  }

  @Override
  public IcsFile next() throws IOException {
    if (imageContainers.size() == 0)
      throw new IOException("No next element available!");

    MultiMediaContainer container = imageContainers.remove(0);

    File icsFileSource = container.getMetaData().get(0);
    String id = createID(root, icsFileSource);
    DDSM_Image leftCC = getByNameEnding(container, "LEFT_CC" + "." + imageFileEnding);
    DDSM_Image leftMLO = getByNameEnding(container, "LEFT_MLO" + "." + imageFileEnding);
    DDSM_Image rightCC = getByNameEnding(container, "RIGHT_CC" + "." + imageFileEnding);
    DDSM_Image rightMLO = getByNameEnding(container, "RIGHT_MLO" + "." + imageFileEnding);

    return IcsFile.parse(icsFileSource,
        id,
        leftCC,
        leftMLO,
        rightCC,
        rightMLO);
  }

  @Override
  public boolean hasNext() throws IOException {
    return imageContainers.size() > 0;
  }

  @Override
  public void close() throws IOException {
      // Nothing has to be closed
  }

  /**
   * TODO
   * @param root
   * @param destination
   * @return
   * @throws UnsupportedEncodingException
   */
  private static String createID(File root, File destination) throws UnsupportedEncodingException {
    return FileUtil.getRelativePath(root, destination);
    //return URLEncoder.encode(id, "UTF-8");
  }

  /**
   * TODO
   * @param source
   * @param ending
   * @return
   */
  private static boolean endsWith(File source, String ending) {
    String name = source.getName();
    return name.matches(".*" + ending);
  }

  /**
   * TODO
   * @param container
   * @param ending
   * @return
   * @throws IOException
   */
  private DDSM_Image getByNameEnding(MultiMediaContainer container, String ending) throws IOException {

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
}