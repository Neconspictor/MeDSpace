package de.unipassau.medspace.common.lucene;

import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * TODO
 */
public interface ResultFactory<ElemType, DocType> {

  /**
   * TODO
   * @param elem
   * @return
   */
  DocType createDoc(ElemType elem);

  /**
   * TODO
   * @param doc
   * @return
   */
  default ElemType createElem(DocType doc) {return null;};

  /**
   * TODO
   * @param elem
   * @return
   */
  List<Triple> triplize(DocType elem);

  Class<DocType> getDocType();
}