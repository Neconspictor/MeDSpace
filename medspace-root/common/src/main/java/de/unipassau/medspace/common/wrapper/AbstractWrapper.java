package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleIndexManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An abtrsact wrapper which implements common used functionality of a wrapper.
 */
public abstract class AbstractWrapper<DocType, ElemType> implements Wrapper {

  /**
   * Used to index data and used for doing keyword searches.
   */
  protected final TripleIndexManager<DocType, ElemType> indexManager;

  /**
   * Allows accessing namespaces by their prefixes.
   */
  protected final Map<String, Namespace> namespaces;

  /**
   * Creates a new AbstractWrapper object.
   * @param indexManager The used index manager, mustn't be null
   * @param namespaces The prefix-namespace mappings
   * @throws IOException if an io error occurs
   */
  public AbstractWrapper(TripleIndexManager<DocType, ElemType> indexManager,
                         Map<String, Namespace> namespaces) throws IOException {
    this.indexManager = indexManager;
    this.namespaces = namespaces;

    // open the index
    Index index = indexManager.getIndex();
    if (index == null) throw new NullPointerException("Index provided by index manager mustn't be null!");
    index.open();
  }


  @Override
  public KeywordSearcher<Triple> createKeywordSearcher(KeywordSearcher.Operator operator) throws IOException {
    try {
      return indexManager.createTripleKeywordSearcher(operator);
    } catch (IOException e) {
      throw new IOException("Error while trying to create a keyword searcher", e);
    }
  }

  @Override
  public boolean existsIndex() {
    return indexManager.getIndex().exists();
  }

  @Override
  public Index getIndex() {
    return indexManager.getIndex();
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return namespaces.values().stream().collect(Collectors.toSet());
  }

  @Override
  public boolean isIndexUsed() {
    return true;
  }
}