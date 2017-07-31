package de.unipassau.medspace.common.stream;

import de.unipassau.medspace.ByteArrayOutputStreamEx;
import de.unipassau.medspace.ResettableByteArrayInputStream;
import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
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
  private final ByteArrayOutputStreamEx out;
  private StreamRDF rdfOut;

  public JenaRDFInputStream(DataSourceStream<Triple> triples, PrefixMapping mapping) {
    this.triples = triples;
    in = new ResettableByteArrayInputStream();
    out = new ByteArrayOutputStreamEx();

    rdfOut = StreamRDFWriter.getWriterStream(out, Lang.TURTLE);
    rdfOut.start();
    if (mapping != null ) {
      addMapping(mapping);
    }
  }

  private void addMapping(PrefixMapping mapping) {
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
      updateInputBuffer();

      if (!triples.hasNext()) {
        rdfOut.finish();
        updateInputBuffer();
        return in.read();
      }
      rdfOut.triple(triples.next());
    }

    return elem;
  }

  private void updateInputBuffer() {
    int pos = out.size();
    if (pos == 0) return;

    if (in.available() != 0) throw new IllegalStateException("Not all bytes consumed yet!");
    in.reset(out.getBufferSource(), 0, pos);
    out.reset();
  }
}