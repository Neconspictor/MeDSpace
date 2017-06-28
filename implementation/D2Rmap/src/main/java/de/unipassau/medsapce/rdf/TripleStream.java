package de.unipassau.medsapce.rdf;

import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Created by David Goeth on 28.06.2017.
 */
public interface TripleStream extends Closeable, Iterable<Triple>, Iterator<Triple> {
}
