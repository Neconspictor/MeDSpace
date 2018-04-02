package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.PdfFileMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.javatuples.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A RDF class adapter for PDF files
 */
public class PdfFileAdapter extends LuceneClassAdapter<PdfFile> {

  private static final String SOURCE = "SOURCE";

  private static final String CONTENT = "CONTENT";


  private final String downloadService;

  /**
   * A list of fields that should be considered for searching but are not used for exporting RDF data.
   */
  protected List<String> notExportedSearchableFields;

  /**
   * Creates a new PdfFileAdapter object.
   * @param pdfFileMapping the rdf mapping for pdf files
   * @param downloadService the base URL of the file download service.
   */
  public PdfFileAdapter(PdfFileMapping pdfFileMapping, String downloadService) {

    super(pdfFileMapping, null);
    this.downloadService = downloadService;
    this.notExportedSearchableFields = new ArrayList<>();


    addPair(SOURCE, pdfFileMapping.getSource());
    addNotExportableSearchableField(CONTENT);

  }

  /**
   * Provides the list of not exported but searchable fields.
   * @return the list of not exported but searchable fields.
   */
  public List<String> getNotExportedSearchableFields() {
    return Collections.unmodifiableList(notExportedSearchableFields);
  }

  @Override
  protected void addFields(PdfFile source, Document doc) throws IOException {
    //String path = source.getSource().getCanonicalPath();
    //path = path.replaceAll("\\\\", "/");
    doc.add(createField(SOURCE, downloadService + source.getId()));

    String extractedText = extractText(source);
    doc.add(createField(CONTENT, extractedText));
  }


  /**
   * Adds a field to the list of not exported but searchable fields.
   * @param fieldName a field that should be searchable ,but not exported.
   */
  protected void addNotExportableSearchableField(String fieldName) {
    notExportedSearchableFields.add(fieldName);
  }

  @Override
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    return null;
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