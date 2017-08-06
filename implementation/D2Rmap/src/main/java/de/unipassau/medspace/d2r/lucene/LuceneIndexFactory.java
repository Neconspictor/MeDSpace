package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.indexing.IndexFactory;
import de.unipassau.medspace.common.lucene.LuceneDataSourceIndex;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class LuceneIndexFactory implements IndexFactory<Document, MappedSqlTuple>{

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
  public DataSourceIndex<Document, MappedSqlTuple> createIndex() throws IOException {
    LuceneD2rResultFactory factory = new LuceneD2rResultFactory(D2R.MAP_FIELD, wrapper);
    return LuceneDataSourceIndex.create(directory, createFields(wrapper.getMaps()), factory);
  }

  /**
   * TODO
   * @param maps
   * @return
   */
  private List<String> createFields(List<D2rMap> maps) {
    List<String> fields = new ArrayList<>();
    for (D2rMap map : maps) {
      List<String> mappedColumns = AbstractD2rResultFactory.getMappedColumns(map);
      fields.addAll(mappedColumns);
    }
    return fields;
  }
}