package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.PdfFileMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.javatuples.Pair;
import java.io.IOException;

/**
 * TODO
 */
public class PdfFileAdapter extends LucenePdfFileDocAdapter<PdfFile> {

  /**
   * TODO
   */
  public static final String SOURCE = "SOURCE";

  /**
   * TODO
   */
  public static final String CONTENT = "CONTENT";


  private final String downloadService;

  /**
   * TODO
   * @param pdfFileParsing
   * @param downloadService
   */
  public PdfFileAdapter(PdfFileMapping pdfFileParsing, String downloadService) {

    super(pdfFileParsing);
    this.downloadService = downloadService;


    addPair(SOURCE, pdfFileParsing.getSource());
    addNotExportableSearchableField(CONTENT);

  }

  @Override
  protected void addFields(PdfFile source, Document doc) throws IOException {
    //String path = source.getSource().getCanonicalPath();
    //path = path.replaceAll("\\\\", "/");
    doc.add(createField(SOURCE, downloadService + source.getId()));

    String extractedText = extractText(source);
    doc.add(createField(CONTENT, extractedText));
  }

  @Override
  public String createValue(Pair<String, PropertyMapping> pair, IndexableField field) {
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