package de.unipassau.medspace;

import org.apache.jena.atlas.io.IndentedWriter;

import java.io.Writer;

/**
 * Created by David Goeth on 30.07.2017.
 */
public class IndentedWriterEx extends IndentedWriter {

  public  IndentedWriterEx(Writer writer) {
    super(writer);
  }
}
