package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriter;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A triple writer for RDF4J.
 */
public class RDF4JTripleWriter implements TripleWriter {

  private static final Logger log = LoggerFactory.getLogger(RDF4JTripleWriter.class);

  private final RDFWriter writer;
  private Writer writerInternal;
  private final StatementFactory factory;
  private final OutputStream out;
  private boolean closed = false;

  /**
   * Creates a new RDF4JTripleWriter object.
   * @param out The output stream used for writing.
   * @param format The language format to use.
   * @throws IOException If any IO error occurs.
   */
  public RDF4JTripleWriter(OutputStream out, RDFFormat format) throws IOException {

    writerInternal = new OutputStreamWriter(out);

    writer = Rio.createWriter(format, out)
                .setWriterConfig(new WriterConfig());


    try{
      writer.startRDF();
    } catch (RuntimeException e) {
      silentClose(writer);
      throw new IOException("Couldn't start RDF writer stream", e);
    }
    factory = new StatementFactory();
    this.out = out;
  }

  private void silentClose(RDFWriter writer) {
    try{
      writer.endRDF();
    } catch (RuntimeException e) {
      log.debug("Error while trying to end RDF writing in silent mode", e);
    }
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void write(Triple triple) throws IOException {
    Statement statement = factory.createFromWrapped(triple);
    try {
      writer.handleStatement(statement);
    } catch (RuntimeException e) {
      throw new IOException("Couldn't write triple", e);
    } finally {
      writerInternal.flush();
      out.flush();

    }
  }

  @Override
  public void write(String prefix, String iri) throws IOException {
    try {
      writer.handleNamespace(prefix, iri);
    } catch (RuntimeException e) {
      throw new IOException("Couldn't write namespace", e);
    } finally {
      writerInternal.flush();
      out.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if (closed) return;

    try{
      writer.endRDF();
      writerInternal.flush();
      out.flush();
      writerInternal.close();
    } catch (RuntimeException e) {
      silentClose(writer);
      throw new IOException("Couldn't end RDF writer stream", e);
    } finally {
      closed = true;
    }
  }
}