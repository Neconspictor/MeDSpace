package de.unipassau.medspace.common.wrapper;

import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public interface TripleConverter<E> {

  List<Triple> convert(E elem);
}
