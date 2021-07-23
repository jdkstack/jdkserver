package org.jdkstack.jdkserver.tcp.core.ssl.socket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.BufType;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.SslHandler;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.WrapperResult;

/**
 * 使用SSL对SocketChannel封装.
 *
 * <p>当使用SocketChannel读数据之后,先对数据进行解密.
 *
 * <pre>
 *   解密后的数据为明文数据,然后将明文数据传输到业务处理.
 * </pre>
 *
 * @author admin
 */
public class SslSocketChannelInputStream extends InputStream {
  SslHandler sslStreams;
  SSLEngine engine;
  int app_buf_size;
  int packet_buf_size;
  ByteBuffer bbuf;
  boolean closed = false;
  /* this stream eof */
  boolean eof = false;

  boolean needData = true;
  byte single[] = new byte[1];

  public SslSocketChannelInputStream(SslHandler sslStreams) {
    this.sslStreams = sslStreams;
    this.engine = sslStreams.getSSLEngine();
    bbuf = allocate(BufType.APPLICATION);
  }

  public int available() throws IOException {
    return bbuf.remaining();
  }

  public boolean markSupported() {
    return false; /* not possible with SSLEngine */
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public long skip(long s) throws IOException {
    int n = (int) s;
    if (closed) {
      throw new IOException("SSL stream is closed");
    }
    if (eof) {
      return 0;
    }
    int ret = n;
    while (n > 0) {
      if (bbuf.remaining() >= n) {
        bbuf.position(bbuf.position() + n);
        return ret;
      } else {
        n -= bbuf.remaining();
        bbuf.clear();
        WrapperResult r = sslStreams.recvData(bbuf);
        bbuf = r.buf == bbuf ? bbuf : r.buf;
      }
    }
    return ret; /* not reached */
  }

  /**
   * close the SSL connection. All data must have been consumed before this is called. Otherwise an
   * exception will be thrown. [Note. May need to revisit this. not quite the normal close()
   * symantics
   */
  public void close() throws IOException {
    eof = true;
    engine.closeInbound();
  }

  public int read(byte[] buf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("SSL stream is closed");
    }
    if (eof) {
      return 0;
    }
    int available = 0;
    if (!needData) {
      available = bbuf.remaining();
      needData = (available == 0);
    }
    if (needData) {
      bbuf.clear();
      WrapperResult r = sslStreams.recvData(bbuf);
      bbuf = r.buf == bbuf ? bbuf : r.buf;
      if ((available = bbuf.remaining()) == 0) {
        eof = true;
        return 0;
      } else {
        needData = false;
      }
    }
    /* copy as much as possible from buf into users buf */
    if (len > available) {
      len = available;
    }
    bbuf.get(buf, off, len);
    return len;
  }

  public int read(byte[] buf) throws IOException {
    return read(buf, 0, buf.length);
  }

  public int read() throws IOException {
    int n = read(single, 0, 1);
    if (n == 0) {
      return -1;
    } else {
      return single[0] & 0xFF;
    }
  }

  private ByteBuffer allocate(BufType type) {
    return allocate(type, -1);
  }

  private ByteBuffer allocate(BufType type, int len) {
    synchronized (this) {
      int size;
      if (type == BufType.PACKET) {
        if (packet_buf_size == 0) {
          SSLSession sess = engine.getSession();
          packet_buf_size = sess.getPacketBufferSize();
        }
        if (len > packet_buf_size) {
          packet_buf_size = len;
        }
        size = packet_buf_size;
      } else {
        if (app_buf_size == 0) {
          SSLSession sess = engine.getSession();
          app_buf_size = sess.getApplicationBufferSize();
        }
        if (len > app_buf_size) {
          app_buf_size = len;
        }
        size = app_buf_size;
      }
      return ByteBuffer.allocate(size);
    }
  }
}
