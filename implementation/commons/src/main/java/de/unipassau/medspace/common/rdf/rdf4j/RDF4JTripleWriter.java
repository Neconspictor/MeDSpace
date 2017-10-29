package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriter;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class RDF4JTripleWriter implements TripleWriter {

  private static final Logger log = LoggerFactory.getLogger(RDF4JTripleWriter.class);

  private final OutputStream out;
  private final RDFFormat format;
  private final RDFWriter writer;
  private final StatementFactory factory;

  public RDF4JTripleWriter(OutputStream out, RDFFormat format) throws IOException {
    this.out = out;
    this.format = format;
    writer = Rio.createWriter(format, out);

    writer.setWriterConfig(new WriterConfig());

    try{
      writer.startRDF();
    } catch (RDFHandlerException e) {
      silentClose(writer);
      throw new IOException("Couldn't start RDF writer stream", e);
    }
    factory = new StatementFactory();
  }

  private void silentClose(RDFWriter writer) {
    try{
      writer.endRDF();
    } catch (RDFHandlerException e) {
      log.debug("Error while trying to end RDF writing in silent mode", e);
    }
  }

  @Override
  public void write(Triple triple) throws IOException {
    Statement statement = factory.create(triple);
    writer.handleStatement(statement);
  }

  @Override
  public void write(String prefix, String iri) throws IOException {
    writer.handleNamespace(prefix, iri);
  }

  @Override
  public void close() throws IOException {
    try{
      writer.endRDF();
    } catch (RDFHandlerException e) {
      silentClose(writer);
      throw new IOException("Couldn't end RDF writer stream", e);
    }
  }
}