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
 * TODO
 */
public class LuceneIndexFactory implements TripleIndexFactory<Document, MappedSqlTuple> {

  private D2rWrapper<Document> wrapper;
  private String directory;

  public LuceneIndexFactory(D2rWrapper<Document> wrapper, String directory) {
    this.wrapper = wrapper;
    this.directory = directory;
  }

  /**
   * TODO
   * @return
   * @throws IOException
   */
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
   * TODO
   * @param maps
   * @return
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
   * TODO
   * @return
   * @throws IOException
   */
  private Analyzer buildAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
        .withTokenizer("whitespace")
        .addTokenFilter("lowercase")
        .addTokenFilter("standard")
        .build();
  }

  private static class LuceneIndexSearcher extends IndexSearcher<Document> {
    private List<String> fields;
    private IndexReaderFactory readerFactory;
    private AnalyzerBuilder builder;

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

  private static class TripleSearchConverter implements Converter<KeywordSearcher<Document>,
      KeywordSearcher<Triple>> {
    private Converter<Document, List<Triple>> triplizer;

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