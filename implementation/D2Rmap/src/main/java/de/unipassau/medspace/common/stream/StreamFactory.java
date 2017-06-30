package de.unipassau.medspace.common.stream;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public interface StreamFactory<E> {

  DataSourceStream<E> create() throws IOException;
}
