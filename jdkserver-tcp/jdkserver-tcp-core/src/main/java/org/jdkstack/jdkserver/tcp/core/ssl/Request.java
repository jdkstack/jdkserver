package org.jdkstack.jdkserver.tcp.core.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/** */
public class Request {

  static final int BUF_LEN = 2048;
  static final byte CR = 13;
  static final byte LF = 10;
  char[] buf = new char[BUF_LEN];
  int pos;
  StringBuffer lineBuf;
  private String startLine;
  private SocketChannel chan;
  private InputStream is;
  private OutputStream os;

  public Request(InputStream rawInputStream, OutputStream rawout) throws IOException {
    is = rawInputStream;
    os = rawout;
    do {
      startLine = readLine();
      if (startLine == null) {
        return;
      }
      /* skip blank lines */
    } while (startLine == null ? false : startLine.equals(""));
  }

  public InputStream inputStream() {
    return is;
  }

  public OutputStream outputStream() {
    return os;
  }

  /** read a line from the stream returning as a String. Not used for reading headers. */
  public String readLine() throws IOException {
    boolean gotCR = false, gotLF = false;
    pos = 0;
    lineBuf = new StringBuffer();
    while (!gotLF) {
      int c = is.read();
      if (c == -1) {
        return null;
      }
      if (gotCR) {
        if (c == LF) {
          gotLF = true;
        } else {
          gotCR = false;
          consume(CR);
          consume(c);
        }
      } else {
        if (c == CR) {
          gotCR = true;
        } else {
          consume(c);
        }
      }
    }
    lineBuf.append(buf, 0, pos);
    return new String(lineBuf);
  }

  private void consume(int c) {
    if (pos == BUF_LEN) {
      lineBuf.append(buf);
      pos = 0;
    }
    buf[pos++] = (char) c;
  }

  /** returns the request line (first line of a request) */
  public String requestLine() {
    return startLine;
  }

  /** Implements blocking reading semantics on top of a non-blocking channel */
  static class ReadStream extends InputStream {
    static final int BUFSIZE = 8 * 1024;
    static long readTimeout;
    SocketChannel channel;
    ByteBuffer chanbuf;
    byte[] one;
    ByteBuffer markBuf; /* reads may be satisfied from this buffer */
    boolean marked;
    boolean reset;
    int readlimit;
    private boolean closed = false, eof = false;

    public ReadStream(SocketChannel chan) throws IOException {
      this.channel = chan;
      chanbuf = ByteBuffer.allocate(BUFSIZE);
      chanbuf.clear();
      one = new byte[1];
      closed = marked = reset = false;
    }

    public synchronized int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    public synchronized int read() throws IOException {
      int result = read(one, 0, 1);
      if (result == 1) {
        return one[0] & 0xFF;
      } else {
        return -1;
      }
    }

    public synchronized int read(byte[] b, int off, int srclen) throws IOException {

      int canreturn, willreturn;

      if (closed) throw new IOException("Stream closed");

      if (eof) {
        return -1;
      }

      assert channel.isBlocking();

      if (off < 0 || srclen < 0 || srclen > (b.length - off)) {
        throw new IndexOutOfBoundsException();
      }

      if (reset) {
        /* satisfy from markBuf */
        canreturn = markBuf.remaining();
        willreturn = canreturn > srclen ? srclen : canreturn;
        markBuf.get(b, off, willreturn);
        if (canreturn == willreturn) {
          reset = false;
        }
      } else {
        /* satisfy from channel */
        chanbuf.clear();
        if (srclen < BUFSIZE) {
          chanbuf.limit(srclen);
        }
        do {
          willreturn = channel.read(chanbuf);
        } while (willreturn == 0);
        if (willreturn == -1) {
          eof = true;
          return -1;
        }
        chanbuf.flip();
        chanbuf.get(b, off, willreturn);

        if (marked) {
          /* copy into markBuf */
          try {
            markBuf.put(b, off, willreturn);
          } catch (BufferOverflowException e) {
            marked = false;
          }
        }
      }
      return willreturn;
    }

    public boolean markSupported() {
      return true;
    }

    /* Does not query the OS socket */
    public synchronized int available() throws IOException {
      if (closed) throw new IOException("Stream is closed");

      if (eof) return -1;

      if (reset) return markBuf.remaining();

      return chanbuf.remaining();
    }

    public void close() throws IOException {
      if (closed) {
        return;
      }
      channel.close();
      closed = true;
    }

    public synchronized void mark(int readlimit) {
      if (closed) return;
      this.readlimit = readlimit;
      markBuf = ByteBuffer.allocate(readlimit);
      marked = true;
      reset = false;
    }

    public synchronized void reset() throws IOException {
      if (closed) return;
      if (!marked) throw new IOException("Stream not marked");
      marked = false;
      reset = true;
      markBuf.flip();
    }
  }

  static class WriteStream extends OutputStream {
    SocketChannel channel;
    ByteBuffer buf;
    SelectionKey key;
    boolean closed;
    byte[] one;

    public WriteStream(SocketChannel channel) throws IOException {
      this.channel = channel;
      assert channel.isBlocking();
      closed = false;
      one = new byte[1];
      buf = ByteBuffer.allocate(4096);
    }

    public synchronized void write(int b) throws IOException {
      one[0] = (byte) b;
      write(one, 0, 1);
    }

    public synchronized void write(byte[] b) throws IOException {
      write(b, 0, b.length);
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
      int l = len;
      if (closed) throw new IOException("stream is closed");

      int cap = buf.capacity();
      if (cap < len) {
        int diff = len - cap;
        buf = ByteBuffer.allocate(2 * (cap + diff));
      }
      buf.clear();
      buf.put(b, off, len);
      buf.flip();
      int n;
      while ((n = channel.write(buf)) < l) {
        l -= n;
        if (l == 0) return;
      }
    }

    public void close() throws IOException {
      if (closed) return;
      // server.logStackTrace ("Request.OS.close: isOpen="+channel.isOpen());
      channel.close();
      closed = true;
    }
  }
}
