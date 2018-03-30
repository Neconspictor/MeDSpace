package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.multimedia.MultiMediaCollector;
import de.unipassau.medspace.common.multimedia.MultiMediaContainer;
import de.unipassau.medspace.common.multimedia.MultiMediaFile;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * A stream that collects all ICS files from a given root folder.
 */
public class IcsFileCollectorStream implements Stream<IcsFile> {

  private final File root;

  private final List<MultiMediaContainer> imageContainers;

  private final String imageFileEnding;

  /**
   * Creates a new IcsFileCollectorStream object.
   *
   * @param root The root folder used as a starting point to collect the ICS files.
   * @param imageFileEnding The file ending of the images specified in the ICS files.
   * @throws IOException If an IO error occurs.
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
    String caseName = getCaseName(icsFileSource);
    Image leftCC = getByNameEnding(container, "LEFT_CC" + "." + imageFileEnding, caseName);
    Image leftMLO = getByNameEnding(container, "LEFT_MLO" + "." + imageFileEnding, caseName);
    Image rightCC = getByNameEnding(container, "RIGHT_CC" + "." + imageFileEnding, caseName);
    Image rightMLO = getByNameEnding(container, "RIGHT_MLO" + "." + imageFileEnding, caseName);

    return IcsFile.parse(icsFileSource,
        id,
        caseName,
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


  private static String createID(File root, File destination) throws UnsupportedEncodingException {
    return FileUtil.getRelativePath(root, destination);
    //return URLEncoder.encode(id, "UTF-8");
  }


  private static boolean endsWith(File source, String ending) {
    String name = source.getName();
    return name.matches(".*" + ending);
  }

  private Image getByNameEnding(MultiMediaContainer container, String ending, String caseName) throws IOException {

    for (MultiMediaFile multiMediaFile: container.getData()) {
      File source = multiMediaFile.getSource();
      if (endsWith(source, ending)) {

        OverlayMetaData overlayMetaData = null;
        if (multiMediaFile.getMetaData().size() > 0) {
          File overlay = multiMediaFile.getMetaData().get(0);
          String overlayID = createID(root, overlay);
          overlayMetaData = OverlayMetaData.parse(overlay, overlayID, caseName);
        }

        String id = createID(root, source);

        return new Image(source, overlayMetaData, id, caseName);
      }
    }

    throw new IOException("Couldn't create DDSM_Image by name ending search: '" + ending + "'");
  }
  
  private String getCaseName(File file) {
    assert file.isFile();
    File folder = file.getParentFile();
    return folder.getName();
  }
}