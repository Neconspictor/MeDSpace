package de.unipassau.medspace.test;

import java.io.*;

/**
 * Created by David Goeth on 29.07.2017.
 */
public class InputOutputStream  {
  private ByteArrayInputStreamEx in;
  private ByteArrayOutputStreamEx out;
  private byte[] buffer;

  public InputOutputStream(int bufferSize) throws IOException {
    out = new ByteArrayOutputStreamEx(bufferSize);
    in = new ByteArrayInputStreamEx(out.getBuffer());
    buffer = out.getBuffer();
  }


  public String getByteContent() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < buffer.length; ++i) {
      builder.append(buffer[i]);
      builder.append("\n");
    }
    return builder.toString();
  }

  public InputStream getIn() {
    return in;
  }

  public OutputStream getOut() {
    return out;
  }

  private static class ByteArrayInputStreamEx extends ByteArrayInputStream {

    public ByteArrayInputStreamEx(byte[] buf) {
      super(buf);
    }

    public ByteArrayInputStreamEx(byte[] buf, int offset, int length) {
      super(buf, offset, length);
    }

    public int getPos() {
      return pos;
    }

    public int getMark() {
      return mark;
    }

    public int getCount() {
      return count;
    }
  }

  private static class ByteArrayOutputStreamEx extends ByteArrayOutputStream {

    public ByteArrayOutputStreamEx() {
      super();
    }

    public ByteArrayOutputStreamEx(int bufferSize) {
      super(bufferSize);
    }

    public byte[] getBuffer() {
      return buf;
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public synchronized void write(int b) {
      if (count >= buf.length)
        throw new IndexOutOfBoundsException("Write buffer is full");
      buf[count] = (byte) b;
      count += 1;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public synchronized void write(byte b[], int off, int len) {
      if ((off < 0) || (off > b.length) || (len < 0) ||
          ((off + len + count) - b.length >= 0)){
        throw new IndexOutOfBoundsException("Not enough free space available in the write-buffer");
      }
      System.arraycopy(b, off, buf, count, len);
      count += len;
    }

  }

}
