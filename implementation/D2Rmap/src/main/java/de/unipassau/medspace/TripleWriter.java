package de.unipassau.medspace;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by David Goeth on 30.07.2017.
 */
public class TripleWriter extends Writer {
  private byte[] buffer;
  private int pos = -1;

  public TripleWriter(int bufferSize) {
    buffer = new byte[bufferSize];
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    byte[] bytes = fromCharArray(cbuf);
    System.arraycopy(bytes, off, buffer, pos + 1, len);
    pos += len;
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }

  public byte[] getBuffer() {
    return buffer;
  }

  public int getPos() {
    return pos;
  }

  public void reset() {
    pos = -1;
  }

  private static byte[] fromCharArray(char[] cbuf) {
    return new String(cbuf).getBytes();
  }
}