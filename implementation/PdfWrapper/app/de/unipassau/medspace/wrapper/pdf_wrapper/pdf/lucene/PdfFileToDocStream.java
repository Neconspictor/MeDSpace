package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.PdfFileAdapter;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * TODO
 */
public class PdfFileToDocStream implements Stream<Document> {

  /**
   * TODO
   */
  private final Stream<PdfFile> source;

  /**
   * TODO
   */
  private final PdfFileAdapter pdfFileAdapter;

  /**
   * TODO
   * @param source
   * @param pdfFileAdapter
   * @throws IOException
   */
  public PdfFileToDocStream(Stream<PdfFile> source,
                            PdfFileAdapter pdfFileAdapter) {
    this.source = source;
    this.pdfFileAdapter = pdfFileAdapter;
  }

  /**
   * TODO
   * @param pdfFile
   * @return
   * @throws IOException
   */
  private Document createDocs(PdfFile pdfFile) throws IOException {

    //icsFile
    Document doc = pdfFileAdapter.convert(pdfFile);
    return doc;
  }

  @Override
  public Document next() throws IOException {
    if (!hasNext()) throw new IOException("No next element available!");
    PdfFile nextPdfFile = source.next();
    return createDocs(nextPdfFile);
  }

  @Override
  public boolean hasNext() throws IOException {
    return source.hasNext();
  }

  @Override
  public void close() throws IOException {
    // Nothing to do
  }
}