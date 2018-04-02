package de.unipassau.medspace.data_collector.rdf4j;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.*;
import org.eclipse.rdf4j.rio.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * A wrapper for a repository connection. It is used to execute callback methods when the connection is closed.
 */
public class ConnectionForwarder implements RepositoryConnection {

  private RepositoryConnection impl;

  private Runnable onCloseCallback;

  /**
   * Creates a new ConnectionForwarder object.
   * @param source The respository connection
   * @param onClose Called when the repository connection gets closed.
   */
  public ConnectionForwarder(RepositoryConnection source, Runnable onClose) {
    impl = source;
    onCloseCallback = onClose;
  }

  //changed methods

  @Override
  public Repository getRepository() {
    throw new UnsupportedOperationException("This method is not supported!");
  }

  @Override
  public void close() {
    impl.close();
    onCloseCallback.run();
  }


  // forwarded methods

  @Override
  public void setParserConfig(ParserConfig config) {
    impl.setParserConfig(config);
  }

  @Override
  public ParserConfig getParserConfig() {
    return impl.getParserConfig();
  }

  @Override
  public ValueFactory getValueFactory() {
    return impl.getValueFactory();
  }

  @Override
  public boolean isOpen() throws RepositoryException {
    return impl.isOpen();
  }

  @Override
  public Query prepareQuery(QueryLanguage ql, String query) throws RepositoryException, MalformedQueryException {
    return impl.prepareQuery(ql, query);
  }

  @Override
  public Query prepareQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException, MalformedQueryException {
    return impl.prepareQuery(ql, query, baseURI);
  }

  @Override
  public TupleQuery prepareTupleQuery(QueryLanguage ql, String query) throws RepositoryException, MalformedQueryException {
    return impl.prepareTupleQuery(ql, query);
  }

  @Override
  public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException, MalformedQueryException {
    return impl.prepareTupleQuery(ql, query, baseURI);
  }

  @Override
  public GraphQuery prepareGraphQuery(QueryLanguage ql, String query) throws RepositoryException, MalformedQueryException {
    return impl.prepareGraphQuery(ql, query);
  }

  @Override
  public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException, MalformedQueryException {
    return impl.prepareGraphQuery(ql, query, baseURI);
  }

  @Override
  public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query) throws RepositoryException, MalformedQueryException {
    return impl.prepareBooleanQuery(ql, query);
  }

  @Override
  public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException, MalformedQueryException {
    return impl.prepareBooleanQuery(ql, query, baseURI);
  }

  @Override
  public Update prepareUpdate(QueryLanguage ql, String update) throws RepositoryException, MalformedQueryException {
    return impl.prepareUpdate(ql, update);
  }

  @Override
  public Update prepareUpdate(QueryLanguage ql, String update, String baseURI) throws RepositoryException, MalformedQueryException {
    return impl.prepareUpdate(ql, update, baseURI);
  }

  @Override
  public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
    return impl.getContextIDs();
  }

  @Override
  public RepositoryResult<Statement> getStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) throws RepositoryException {
    return impl.getStatements(subj, pred, obj);
  }

  @Override
  public boolean hasStatement(Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) throws RepositoryException {
    return impl.hasStatement(subj, pred, obj, includeInferred, contexts);
  }

  @Override
  public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts) throws RepositoryException {
    return impl.hasStatement(st, includeInferred, contexts);
  }

  @Override
  public void exportStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, RDFHandler handler, Resource... contexts) throws RepositoryException, RDFHandlerException {
    impl.exportStatements(subj, pred, obj, includeInferred, handler, contexts);
  }

  @Override
  public void export(RDFHandler handler, Resource... contexts) throws RepositoryException, RDFHandlerException {
    impl.export(handler, contexts);
  }

  @Override
  public long size(Resource... contexts) throws RepositoryException {
    return impl.size(contexts);
  }

  @Override
  public boolean isEmpty() throws RepositoryException {
    return impl.isEmpty();
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws RepositoryException {
    impl.setAutoCommit(autoCommit);
  }

  @Override
  public boolean isAutoCommit() throws RepositoryException {
    return impl.isAutoCommit();
  }

  @Override
  public boolean isActive() throws UnknownTransactionStateException, RepositoryException {
    return impl.isActive();
  }

  @Override
  public void setIsolationLevel(IsolationLevel level) throws IllegalStateException {
    impl.setIsolationLevel(level);
  }

  @Override
  public IsolationLevel getIsolationLevel() {
    return impl.getIsolationLevel();
  }

  @Override
  public void begin() throws RepositoryException {
    impl.begin();
  }

  @Override
  public void begin(IsolationLevel level) throws RepositoryException {
    impl.begin(level);
  }

  @Override
  public void commit() throws RepositoryException {
    impl.commit();
  }

  @Override
  public void rollback() throws RepositoryException {
    impl.rollback();
  }

  @Override
  public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException, RDFParseException, RepositoryException {
    impl.add(in, baseURI, dataFormat, contexts);
  }

  @Override
  public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException, RDFParseException, RepositoryException {
    impl.add(reader, baseURI, dataFormat, contexts);
  }

  @Override
  public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException, RDFParseException, RepositoryException {
    impl.add(url, baseURI, dataFormat, contexts);
  }

  @Override
  public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException, RDFParseException, RepositoryException {
    impl.add(file, baseURI, dataFormat, contexts);
  }

  @Override
  public void add(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
    impl.add(subject, predicate, object, contexts);
  }

  @Override
  public void add(Statement st, Resource... contexts) throws RepositoryException {
    impl.add(st, contexts);
  }

  @Override
  public void add(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
    impl.add(statements, contexts);
  }

  @Override
  public <E extends Exception> void add(Iteration<? extends Statement, E> statements, Resource... contexts) throws RepositoryException, E {
    impl.add(statements, contexts);
  }

  @Override
  public void remove(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
    impl.remove(subject, predicate, object, contexts);
  }

  @Override
  public void remove(Statement st, Resource... contexts) throws RepositoryException {
    impl.remove(st, contexts);
  }

  @Override
  public void remove(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
    impl.remove(statements, contexts);
  }

  @Override
  public <E extends Exception> void remove(Iteration<? extends Statement, E> statements, Resource... contexts) throws RepositoryException, E {
    impl.remove(statements, contexts);
  }

  @Override
  public void clear(Resource... contexts) throws RepositoryException {
    impl.clear(contexts);
  }

  @Override
  public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
    return impl.getNamespaces();
  }

  @Override
  public String getNamespace(String prefix) throws RepositoryException {
    return impl.getNamespace(prefix);
  }

  @Override
  public void setNamespace(String prefix, String name) throws RepositoryException {
    impl.setNamespace(prefix, name);
  }

  @Override
  public void removeNamespace(String prefix) throws RepositoryException {
    impl.removeNamespace(prefix);
  }

  @Override
  public void clearNamespaces() throws RepositoryException {
    impl.clearNamespaces();
  }
}