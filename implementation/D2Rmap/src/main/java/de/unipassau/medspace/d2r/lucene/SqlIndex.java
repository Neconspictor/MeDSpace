package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.indexing.FullTextSearchIndex;
import de.unipassau.medspace.common.lucene.FullTextSearchIndexImpl;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.exception.D2RException;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Goeth on 23.07.2017.
 */
public class SqlIndex implements DataSourceIndex {

  private static Logger log = Logger.getLogger(SqlIndex.class);

  private FullTextSearchIndexImpl index;
  private D2rProxy proxy;
  private SqlResultFactory factory;

  public SqlIndex(Path directory, D2rProxy proxy, SqlResultFactory factory) throws D2RException {

    this.proxy = proxy;
    this.factory = factory;

    try {
      index = FullTextSearchIndexImpl.create(directory.toString());
      List<String> fields =  createFields(proxy.getMaps());
      index.setSearchableFields(fields);
      index.open();
    } catch (IOException e) {
      log.error(e);
      throw new D2RException("Couldn't create index!");
    }
  }

  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(index, true);
  }

  @Override
  public boolean exists() {
    try {
      return index.hasIndexedData();
    } catch (IOException e) {
      log.error(e);
      return false;
    }
  }

  public FullTextSearchIndex<Document> getFtsIndex() {
    return index;
  }

  @Override
  public void reindex() throws IOException {
    DataSourceStream<Document> docStream = null;
    try {
      docStream = new SqlToDocStream(proxy.getAllData(), factory);
      index.open();
      index.reindex(docStream);

    } catch (IOException e) {
      throw new IOException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }
  }

  private List<String> createFields(List<D2rMap> maps) {
    List<String> fields = new ArrayList<>();
    for (D2rMap map : maps) {
      List<String> mappedColumns = SqlResultFactory.getMappedColumns(map);
      fields.addAll(mappedColumns);
    }
    return fields;
  }
}