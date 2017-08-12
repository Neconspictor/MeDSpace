package de.unipassau.medspace.common.rdf;
import java.io.IOException;

/**
 * TODO
 */
public interface TripleIndexFactory<DocType, ElemType> {

  TripleIndexManager<DocType, ElemType> createIndexManager() throws IOException;
}
