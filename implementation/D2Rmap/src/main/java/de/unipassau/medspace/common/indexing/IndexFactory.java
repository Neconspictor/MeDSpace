package de.unipassau.medspace.common.indexing;

import java.io.IOException;

/**
 * TODO
 */
public interface IndexFactory<DocType, ElemType> {

  Index<DocType, ElemType> createIndex() throws IOException;
}
