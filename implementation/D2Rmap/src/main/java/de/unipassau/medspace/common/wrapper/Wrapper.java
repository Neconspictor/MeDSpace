package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.query.KeywordSearcher;
import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by David Goeth on 24.07.2017.
 */
public interface Wrapper extends Closeable {

  KeywordSearcher<Triple> getKeywordSearcher() throws IOException;
}
