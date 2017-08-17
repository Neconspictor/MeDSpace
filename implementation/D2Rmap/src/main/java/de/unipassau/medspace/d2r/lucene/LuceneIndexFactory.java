package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.lucene.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.jena.graph.Triple;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory that creates a lucene index manager for a {@link D2rWrapper}.
 */
public class LuceneIndexFactory implements TripleIndexFactory<Document, MappedSqlTuple> {

  /**
   * The D2rWrapper used to create the index.
   */
  private D2rWrapper<Document> wrapper;

  /**
   * The directory to store lucene index data to.
   */
  private String directory;

  /**
   * Creates a new LuceneIndexFactory.
   * @param wrapper The wrapper to create an index manager for.
   * @param directory The directory the created index should store its data.
   */
  public LuceneIndexFactory(D2rWrapper<Document> wrapper, String directory) {
    this.wrapper = wrapper;
    this.directory = directory;
  }


  @Override
  public TripleIndexManager<Document, MappedSqlTuple> createIndexManager() throws IOException {

    LuceneD2rResultFactory factory = new LuceneD2rResultFactory(D2R.MAP_FIELD, wrapper);
    List<String> fields = createFields(wrapper.getMaps());
    AnalyzerBuilder builder = () -> buildAnalyzer();
    LuceneIndex index = LuceneIndex.create(directory, builder);
    IndexReaderFactory readerFactory = () -> index.createReader();
    IndexSearcher<Document> searcher = new LuceneIndexSearcher(index, fields, readerFactory, builder);
    TripleSearchConverter converter = new TripleSearchConverter(factory.getTriplizer());

    return new TripleIndexManager<>(searcher,
        factory.getToDoc(), factory.getToElem(), converter);
  }

  /**
   * Creates a list of a storable fields from a given list of D2rMaps.
   * @param maps The D2rMap list to get all storable fields from.
   * @return A list of a storable fields from a given list of D2rMaps.
   */
  private List<String> createFields(List<D2rMap> maps) {
    List<String> fields = new ArrayList<>();
    for (D2rMap map : maps) {
      List<String> mappedColumns = LuceneD2rResultFactory.getMappedColumns(map);
      fields.addAll(mappedColumns);
    }
    return fields;
  }

  /**
   * Creates an {@link Analyzer} used for indexing and searching.
   * @return An {@link Analyzer} used for indexing and searching.
   * @throws IOException If an IO-Error occurs.
   */
  private Analyzer buildAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
        .withTokenizer("whitespace")
        .addTokenFilter("lowercase")
        .addTokenFilter("standard")
        .build();
  }

  /**
   * An index searcher tailored to the lucene search engine.
   */
  private static class LuceneIndexSearcher extends IndexSearcher<Document> {

    /**
     * A list of fields, that should be searchable.
     */
    private List<String> fields;

    /**
     * A factory creating an index reader.
     */
    private IndexReaderFactory readerFactory;

    /**
     * A factory for creating an analyzer for analyzing the search query before executing it.
     */
    private AnalyzerBuilder builder;

    /**
     * Creates a new LuceneIndexSearcher.
     * @param index The index the index searcher should operate on.
     * @param fields A list of fields that are stored in the index and should be searchable by this searcher.
     * @param readerFactory A factory to create a reader for the index.
     * @param builder A factory for creating an analyzer for analyzing the search query before executing it.
     */
    public LuceneIndexSearcher(Index<Document> index, List<String> fields, IndexReaderFactory readerFactory,
                               AnalyzerBuilder builder) {
      super(index);
      this.fields = fields;
      this.readerFactory = readerFactory;
      this.builder = builder;
    }

    @Override
    public KeywordSearcher<Document> createKeywordSearcher() throws IOException {
      return new LuceneKeywordSearcher(fields, readerFactory, builder.build());
    }
  }

  /**
   * Converts a keyword searcher outputing lucene documents to a converter that outputs the result to rdf triples.
   */
  private static class TripleSearchConverter implements Converter<KeywordSearcher<Document>,
      KeywordSearcher<Triple>> {

    /**
     * Used to convert the documents to rdf triples.
     */
    private Converter<Document, List<Triple>> triplizer;

    /**
     * Creates a new TripleSearchConverter.
     * @param triplizer The converter used to create triples from the documents.
     */
    public TripleSearchConverter(Converter<Document, List<Triple>> triplizer) {
      this.triplizer = triplizer;
    }

    @Override
    public KeywordSearcher<Triple> convert(KeywordSearcher<Document> source) {
      return keywords -> {
        Stream<Document> result =  source.searchForKeywords(keywords);
        return new DocToTripleStream(result, triplizer);
      };
    }
  }
}