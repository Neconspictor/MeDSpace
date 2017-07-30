package de.unipassau.medspace;

import java.io.ByteArrayInputStream;

/**
 * Created by David Goeth on 30.07.2017.
 */
public class ResettableByteArrayInputStream extends ByteArrayInputStream {


  public ResettableByteArrayInputStream() {
    super(new byte[0]);
  }

  public ResettableByteArrayInputStream(byte[] buf) {
    super(buf);
  }

  public ResettableByteArrayInputStream(byte[] buf, int offset, int length) {
    super(buf, offset, length);
  }

  public synchronized void reset(byte[] buf) {
    this.buf = buf;
    this.pos = 0;
    this.count = buf.length;
  }

  public synchronized void reset(byte[] buf, int offset, int length) {
    this.buf = buf;
    this.pos = offset;
    this.count = offset + length;
    this.mark = offset;
  }
}
