package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.PdfFileAdapter;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * A stream that converts a stream of PDF files to a stream of documents.
 */
public class PdfFileToDocStream implements Stream<Document> {

  private final Stream<PdfFile> source;

  private final PdfFileAdapter pdfFileAdapter;

  /**
   * Creates a new PdfFileToDocStream object.
   * @param source The PDF files stream.
   * @param pdfFileAdapter Adapter for creating documents from the PDF files.
   */
  public PdfFileToDocStream(Stream<PdfFile> source,
                            PdfFileAdapter pdfFileAdapter) {
    this.source = source;
    this.pdfFileAdapter = pdfFileAdapter;
  }


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