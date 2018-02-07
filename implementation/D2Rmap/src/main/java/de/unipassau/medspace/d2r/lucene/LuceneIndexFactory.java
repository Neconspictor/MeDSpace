package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.lucene.*;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.StreamConverter;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
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
    AnalyzerBuilder builder = () -> LuceneUtil.buildAnalyzer();
    LuceneIndex index = LuceneIndex.create(directory, builder);
    IndexReaderFactory readerFactory = () -> index.createReader();
    IndexSearcher<Document> searcher = new LuceneIndexSearcher(index, fields, readerFactory, builder);
    TripleSearchConverter converter = new TripleSearchConverter(factory.getTriplizer());

    return new TripleIndexManager<Document, MappedSqlTuple>(searcher, converter) {
      @Override
      public Stream<Document> convert(Stream<MappedSqlTuple> source) {
          return new StreamConverter<>(source, factory.getToDoc());
      }
    };
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
}