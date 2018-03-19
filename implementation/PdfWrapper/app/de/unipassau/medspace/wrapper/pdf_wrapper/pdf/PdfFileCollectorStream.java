package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

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
 * TODO
 */
public class PdfFileCollectorStream implements Stream<PdfFile> {

  /**
   * TODO
   */
  private final File root;

  /**
   * TODO
   */
  private final List<MultiMediaContainer> pdfContainers;

  /**
   * TODO
   * @param root
   * @throws IOException
   */
  public PdfFileCollectorStream(File root) throws IOException {

    if (!root.isDirectory()) {
      throw new IOException("root image directory doesn't exist: " + root.getAbsoluteFile());
    }

    this.root = root;

    MultiMediaCollector collector = new PdfFileCollector();
    pdfContainers = collector.collect(root);
  }

  @Override
  public PdfFile next() throws IOException {
    if (pdfContainers.size() == 0)
      throw new IOException("No next element available!");

    MultiMediaContainer container = pdfContainers.remove(0);

    MultiMediaFile multiMediaFile = container.getData().get(0);
    File pdfFileSource = multiMediaFile.getSource();
    String id = createID(root, pdfFileSource);

    return new PdfFile(pdfFileSource, id);
  }

  @Override
  public boolean hasNext() throws IOException {
    return pdfContainers.size() > 0;
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
}