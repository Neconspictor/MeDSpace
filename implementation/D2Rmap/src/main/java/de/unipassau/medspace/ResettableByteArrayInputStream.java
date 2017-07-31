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
    this.buf = new byte[buf.length];
    System.arraycopy(buf, 0, this.buf, 0, buf.length);
    this.pos = 0;
    this.count = buf.length;
  }

  public synchronized void reset(byte[] buf, int offset, int length) {


    this.buf = new byte[length];
    System.arraycopy(buf, offset, this.buf, 0, length);
    this.pos = 0;
    this.count = length;
    this.mark = 0;
  }
}
