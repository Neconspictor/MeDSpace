package de.unipassau.medspace.common.stream;

import java.io.ByteArrayInputStream;

/**
 * This class is an extension to a java.io.ByteArrayInputStream class.
 * It allows to reset the underlying byte buffer with new content.
 *
 * This class is useful e.g. when creating an input stream from a buffered byte output stream.
 * Using a normal ByteArrayInputStream would result in many temporary objects producing unnecessary
 * garbage collection.
 */
public class ResettableByteArrayInputStream extends ByteArrayInputStream {


  /**
   * Creates a new ResettableByteArrayInputStream.
   */
  public ResettableByteArrayInputStream() {
    super(new byte[0]);
  }

  /**
   * Creates a new ResettableByteArrayInputStream and uses the specified byte array as its input data.
   * @param buf The new data that should be readable from this input stream.
   */
  public ResettableByteArrayInputStream(byte[] buf) {
    super(buf);
  }

  /**
   * Creates a new ResettableByteArrayInputStream and uses the specified byte array as its input data.
   * With the offset and length parameter the user states where the stream should start to read data from and
   * where to end it.
   * @param buf the input buffer.
   * @param offset the offset in the buffer of the first byte to read.
   * @param length the maximum number of bytes to read from the buffer.
   */
  public ResettableByteArrayInputStream(byte[] buf, int offset, int length) {
    super(buf, offset, length);
  }

  /**
   * Resets this input stream and fills it with new data.
   * @param buf The new data that this input stream should be initialized with.
   */
  public synchronized void reset(byte[] buf) {
    this.buf = new byte[buf.length];
    System.arraycopy(buf, 0, this.buf, 0, buf.length);
    this.pos = 0;
    this.count = buf.length;
  }

  /**
   * Resets the input stream and fill it with new data from a buffer with a starting offset and a length argument.
   * @param buf he input buffer.
   * @param offset the offset in the buffer of the first byte to read.
   * @param length the maximum number of bytes to read from the buffer.
   */
  public synchronized void reset(byte[] buf, int offset, int length) {
    this.buf = new byte[length];
    System.arraycopy(buf, offset, this.buf, 0, length);
    this.pos = 0;
    this.count = length;
    this.mark = 0;
  }
}