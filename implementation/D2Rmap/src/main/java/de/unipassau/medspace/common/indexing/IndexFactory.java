package de.unipassau.medspace.common.indexing;

import java.io.IOException;

/**
 * TODO
 */
public interface IndexFactory<DocType> {

  DataSourceIndex<DocType> createIndex() throws IOException;
}
