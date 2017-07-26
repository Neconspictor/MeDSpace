package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.d2r.lucene.D2rKeywordSearcher;
import de.unipassau.medspace.d2r.lucene.SqlIndex;
import de.unipassau.medspace.d2r.lucene.SqlResultFactory;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by David Goeth on 24.07.2017.
 */
public class D2rWrapper implements Wrapper {

  private static Logger log = LoggerFactory.getLogger(D2rWrapper.class);

  private D2rProxy proxy;
  private SqlIndex index;
  private SqlResultFactory resultFactory;

  public D2rWrapper(D2rProxy proxy, Path indexDirectory) throws IOException {
    this.proxy = proxy;
    resultFactory = new SqlResultFactory(D2R.MAP_FIELD, proxy);
    try {
      index  = new SqlIndex(indexDirectory, proxy, resultFactory);
    } catch (IOException e) {
      throw new IOException("Couldn't create Index", e);
    }
  }

  @Override
  public void close() throws IOException {
    index.close();
  }

  @Override
  public KeywordSearcher<Triple> createKeywordSearcher() throws IOException {

    D2rKeywordSearcher searcher = null;

    try {
      searcher = new D2rKeywordSearcher(this, index.getFtsIndex().createKeywordSearcher());
    } catch (IOException e) {
      throw new IOException("Error while trying to create a keyword searcher", e);
    }

    searcher.useLucene(true);
    return searcher;
  }

  public DataSourceIndex getIndex() {
    return index;
  }

  public D2rProxy getProxy() {
    return proxy;
  }

  public SqlResultFactory getResultFactory() {
    return resultFactory;
  }
}