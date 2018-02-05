package de.unipassau.medspace.common.wrapper;


import de.unipassau.medspace.common.rdf.Triple;

import java.util.List;

/**
 * A triple converter converts java objects of a given type to a list of rdf triples,
 * that represent the same thing as the java object.
 */
public interface TripleConverter<E> {


  /**
   * Converts a specified object 'elem' to a list of rdf triples,
   * that respresent the same thing.
   *
   * @param elem The object to get a list of rdf triples from
   * @return A list of rdf triples representing 'elem'
   * @throws IllegalArgumentException if 'elem' is null
   */
  List<Triple> convert(E elem);
}
