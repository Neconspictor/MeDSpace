package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.PdfFileParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.PropertyParsing;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.javatuples.Pair;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * TODO
 */
public class PdfFileAdapter extends LuceneDocAdapter<PdfFile> {

  /**
   * TODO
   */
  public static final String SOURCE = "SOURCE";

  /**
   * TODO
   */
  public static final String CONTENT = "CONTENT";

  /**
   * TODO
   * @param pdfFileParsing
   */
  public PdfFileAdapter(PdfFileParsing pdfFileParsing) {

    super(pdfFileParsing);

    addPair(SOURCE, pdfFileParsing.getSource());
    addNotExportableSearchableField(CONTENT);

  }

  @Override
  protected void addFields(PdfFile source, Document doc) throws IOException {
    String path = source.getSource().getCanonicalPath();
    path = path.replaceAll("\\\\", "/");
    doc.add(createField(SOURCE, path));

    String extractedText = extractText(source);
    doc.add(createField(CONTENT, extractedText));
  }

  @Override
  public String createValue(Pair<String, PropertyParsing> pair, IndexableField field) {
    return field.stringValue();
  }

  private String extractText(PdfFile source) throws IOException {
    try (PDDocument document = PDDocument.load(source.getSource())){
      PDFTextStripper pdfStripper = new PDFTextStripper();

      String text = pdfStripper.getText(document);
      document.close();
      return text;
    }
  }
}