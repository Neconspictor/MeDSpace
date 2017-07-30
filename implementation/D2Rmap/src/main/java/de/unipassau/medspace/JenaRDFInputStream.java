package de.unipassau.medspace;

import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
import org.apache.jena.shared.PrefixMapping;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by David Goeth on 30.07.2017.
 */
public class JenaRDFInputStream extends InputStream {

  private final ResettableByteArrayInputStream in;
  private final DataSourceStream<Triple> triples;
  private final TripleWriter writer;
  private StreamRDF rdfOut;

  public JenaRDFInputStream(DataSourceStream<Triple> triples, PrefixMapping mapping, boolean writeMapping) {
    this.triples = triples;
    writer = new TripleWriter(1024);
    in = new ResettableByteArrayInputStream();

    rdfOut =  new WriterStreamRDFBlocks(new IndentedWriterEx(writer));
    rdfOut.start();
    WriterStreamRDFBaseExtension extended = new WriterStreamRDFBaseExtension(rdfOut);
    if (mapping != null ) {
      addMapping(mapping, writeMapping, extended);
    }
  }

  private void addMapping(PrefixMapping mapping, boolean writeMapping, WriterStreamRDFBaseExtension extended) {
    if (!writeMapping) {
      extended.addPrefixMapping(mapping);
      return;
    }

    for (Map.Entry<String, String> map : mapping.getNsPrefixMap().entrySet()) {
      rdfOut.prefix(map.getKey(), map.getValue());
    }
  }


  @Override
  public int read() throws IOException {
    int elem = in.read();

    // elem == -1, i.d. in has to be filled with new data

    while (elem == -1) {
      elem = in.read();
      //if (elem != -1) return elem;
      if (elem != -1) {
       int test = 0; //TODO without return there is a bug, but i don't know why; check it out!
       return elem;
      }
      updateInputBuffer();

      if (!triples.hasNext()) {
        rdfOut.finish();
        return in.read();
      }
      rdfOut.triple(triples.next());
    }

    return elem;
  }

  private void updateInputBuffer() {
    int pos = writer.getPos();
    if (pos == -1) return;

    if (in.available() != 0) throw new IllegalStateException("Not all bytes consumed yet!");
    in.reset(writer.getBuffer(), 0, pos);
    writer.reset();
  }
}