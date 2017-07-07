package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.URINormalizer;
import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public interface Wrapper<E> {

  KeywordSearcher<Triple> createKeywordSearcher();

  TripleConverter<E> getConverter();

  Index getIndex();

  Namespace getNamespaceByPrefix(String prefix);

  List<Namespace> getNamespaces();

  URINormalizer getNormalizer();

  void setNormalizer(URINormalizer normalizer);

  void useIndex(boolean useIt);

}
