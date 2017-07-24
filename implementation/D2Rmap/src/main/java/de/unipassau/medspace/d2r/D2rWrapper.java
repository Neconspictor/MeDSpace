package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.lucene.D2rKeywordSearcher;
import de.unipassau.medspace.d2r.lucene.SqlIndex;
import de.unipassau.medspace.d2r.lucene.SqlResultFactory;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by David Goeth on 24.07.2017.
 */
public class D2rWrapper implements Wrapper {

  private static Logger log = Logger.getLogger(D2rWrapper.class);

  private D2rProxy proxy;
  private SqlIndex index;
  private SqlResultFactory resultFactory;

  public D2rWrapper(D2rProxy proxy, Path indexDirectory) throws D2RException {
    this.proxy = proxy;
    resultFactory = new SqlResultFactory(D2R.MAP_FIELD, proxy);
    index  = new SqlIndex(indexDirectory, proxy, resultFactory);
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
      log.error(e);
      throw new IOException("Couldn't create keyword searcher!");
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