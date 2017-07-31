package de.unipassau.medspace;

import java.io.ByteArrayOutputStream;

/**
 * Created by David Goeth on 31.07.2017.
 */
public class ByteArrayOutputStreamEx extends ByteArrayOutputStream {

  public byte[] getBufferSource() {
    return buf;
  }
}
