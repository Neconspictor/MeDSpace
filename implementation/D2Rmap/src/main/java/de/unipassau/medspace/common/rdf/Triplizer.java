package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.util.Converter;
import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * A triplizer converts onject from type E to a list of jena rdf triples
 */
public abstract class Triplizer<E> implements Converter<E, List<Triple>> {
}