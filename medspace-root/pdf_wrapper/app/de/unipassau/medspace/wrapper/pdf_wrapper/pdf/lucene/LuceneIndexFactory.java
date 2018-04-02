package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene;

import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.lucene.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.common.util.MiskUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.*;
import org.apache.lucene.document.Document;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory for triple an index for documents that contain data from PDF files and that are convertible to RDF.
 */
public class LuceneIndexFactory implements TripleIndexFactory<Document, PdfFile> {

  private final List<PdfFileAdapter> adpaters;
  private final String directory;
  private final RDFFactory factory;
  private final QNameNormalizer normalizer;

  /**
   * Creates a new LuceneIndexFactory object.
   * @param directory The root folder for locating PDf files.
   * @param adpaters The used adapters
   * @param factory Used to create RDF data.
   * @param normalizer Used to normalize RDF data.
   */
  public LuceneIndexFactory(String directory,
                            List<PdfFileAdapter> adpaters,
                            RDFFactory factory,
                            QNameNormalizer normalizer) {
    this.directory = directory;
    this.adpaters = adpaters;
    this.factory = factory;
    this.normalizer = normalizer;
  }

  @Override
  public TripleIndexManager<Document, PdfFile> createIndexManager() throws IOException {

    List<String> fields = createFields();
    AnalyzerBuilder builder = () -> LuceneUtil.buildAnalyzer();
    LuceneIndex index = LuceneIndex.create(directory, builder);
    IndexReaderFactory readerFactory = () -> index.createReader();
    IndexSearcher<Document> searcher = new LuceneIndexSearcher(index, fields, readerFactory, builder);

    Converter<Document, List<Triple>> triplizer = new DocumentClassTriplizer(adpaters, normalizer, factory);
    TripleSearchConverter converter = new TripleSearchConverter(triplizer);

    return new PdfFileTripleIndexManager(searcher, converter, adpaters);
  }

  private List<String> createFields() {
    List<String> fields = new ArrayList<>();
    for (PdfFileAdapter adapter : adpaters) {
      List<Pair<String, PropertyMapping>> pairs = adapter.getFieldNamePropertyPairs();
      for (Pair<String, PropertyMapping> pair : pairs) {
        fields.add(pair.getValue0());
      }

      for (String metaDataField : adapter.getMetaDataFields()) {
        fields.add(metaDataField);
      }

      List<String> notExportableSearchableFields = adapter.getNotExportedSearchableFields();
      fields.addAll(notExportableSearchableFields);
    }
    return fields;
  }

  /**
   * A triple index manager for PDF files.
   */
  private static class PdfFileTripleIndexManager extends TripleIndexManager<Document, PdfFile> {


    private final List<PdfFileAdapter> adapters;

    /**
     * Creates a new TripleIndexManager.
     * @param searcher              Used for searching an {@link de.unipassau.medspace.common.indexing.Index}.
     * @param tripleSearchConverter Used to convert documents to triples.
     * @param adapters The adapters for the PDF files.
     */
    public PdfFileTripleIndexManager(IndexSearcher<Document> searcher,
                                     Converter<KeywordSearcher<Document>, KeywordSearcher<Triple>> tripleSearchConverter,
                                     List<PdfFileAdapter> adapters) {
      super(searcher, tripleSearchConverter);
      this.adapters = adapters;
    }

    @Override
    public Stream<Document> convert(Stream<PdfFile> source) {
      return new PdfFileToDocStream(source,
          MiskUtil.getByClass(adapters, PdfFileAdapter.class));
    }
  }
}