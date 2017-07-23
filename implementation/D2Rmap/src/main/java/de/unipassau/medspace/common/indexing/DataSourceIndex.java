package de.unipassau.medspace.common.indexing;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by David Goeth on 23.07.2017.
 */
public interface DataSourceIndex extends Closeable {

  boolean exists();

  void reindex() throws IOException;
}
