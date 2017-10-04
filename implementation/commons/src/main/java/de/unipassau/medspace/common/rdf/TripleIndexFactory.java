package de.unipassau.medspace.common.rdf;
import java.io.IOException;

/**
 * An interface for {@link TripleIndexManager} factories.<br><br>
 * Generics:<br>
 *   DocType: The document type used by the underlying index
 *   ElemType: An object type from which documents should be created and vice versa.
 */
public interface TripleIndexFactory<DocType, ElemType> {

  /**
   * Builds a new {@link TripleIndexManager}
   * @return the new created {@link TripleIndexManager}
   * @throws IOException If an IO-Error occurs.
   */
  TripleIndexManager<DocType, ElemType> createIndexManager() throws IOException;
}