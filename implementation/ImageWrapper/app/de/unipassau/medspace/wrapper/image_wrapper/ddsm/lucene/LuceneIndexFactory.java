package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.lucene.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.*;
import org.apache.lucene.document.Document;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class LuceneIndexFactory implements TripleIndexFactory<Document, IcsFile> {

  /**
   * TODO
   */
  private final List<LuceneDocAdapter<?>> adpaters;

  /**
   * The directory to store lucene index data to.
   */
  private final String directory;

  /**
   * TODO
   */
  private final RDFFactory factory;

  /**
   * TODO
   */
  private final QNameNormalizer normalizer;

  /**
   * TODO
   * @param directory
   * @param adpaters
   * @param factory
   * @param normalizer
   */
  public LuceneIndexFactory(String directory,
                            List<LuceneDocAdapter<?>> adpaters,
                            RDFFactory factory,
                            QNameNormalizer normalizer) {
    this.directory = directory;
    this.adpaters = adpaters;
    this.factory = factory;
    this.normalizer = normalizer;
  }

  @Override
  public TripleIndexManager<Document, IcsFile> createIndexManager() throws IOException {

    List<String> fields = createFields();
    AnalyzerBuilder builder = () -> LuceneUtil.buildAnalyzer();
    LuceneIndex index = LuceneIndex.create(directory, builder);
    IndexReaderFactory readerFactory = () -> index.createReader();
    IndexSearcher<Document> searcher = new LuceneIndexSearcher(index, fields, readerFactory, builder);

    Converter<Document, List<Triple>> triplizer = new DocumentClassTriplizer(adpaters, normalizer, factory);
    TripleSearchConverter converter = new TripleSearchConverter(triplizer);

    return new IcsFileTripleIndexManager(searcher, converter, adpaters);
  }

  /**
   * TODO
   * @return
   */
  private List<String> createFields() {
    List<String> fields = new ArrayList<>();
    for (LuceneDocAdapter<?> adapter : adpaters) {
      List<Pair<String, Property>> pairs = adapter.getFieldNamePropertyPairs();
      for (Pair<String, Property> pair : pairs) {
        fields.add(pair.getValue0());
      }
    }
    return fields;
  }

  /**
   * TODO
   */
  private static class IcsFileTripleIndexManager extends TripleIndexManager<Document, IcsFile> {

    /**
     * TODO
     */
    private final List<LuceneDocAdapter<?>> adapters;

    /**
     * Creates a new TripleIndexManager.
     * @param searcher              Used for searching an {@link de.unipassau.medspace.common.indexing.Index}.
     * @param tripleSearchConverter Used to convert documents to triples.
     * @param adapters TODO
     */
    public IcsFileTripleIndexManager(IndexSearcher<Document> searcher,
                                     Converter<KeywordSearcher<Document>, KeywordSearcher<Triple>> tripleSearchConverter,
                                     List<LuceneDocAdapter<?>> adapters) {
      super(searcher, tripleSearchConverter);
      this.adapters = adapters;
    }

    @Override
    public Stream<Document> convert(Stream<IcsFile> source) {
      Stream<Stream<Document>> streamOfDocumentStreams = new IcsFileStreamToDocStream(source,
          Util.getByClass(adapters, IcsFileAdapter.class),
          Util.getByClass(adapters, OverlayAdapter.class),
          Util.getByClass(adapters, AbnormalityAdapter.class),
          Util.getByClass(adapters, ImageAdapter.class),
          Util.getByClass(adapters, CalcificationAdapter.class),
          Util.getByClass(adapters, MassAdapter.class));

      return new FlatMapStream<>(streamOfDocumentStreams);
    }
  }
}