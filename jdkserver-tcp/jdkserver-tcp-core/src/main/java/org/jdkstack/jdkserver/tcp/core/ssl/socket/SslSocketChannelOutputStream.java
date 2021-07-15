package org.jdkstack.jdkserver.tcp.core.ssl.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.BufType;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.SslHandler;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.WrapperResult;

/**
 * 使用SSL对SocketChannel封装.
 *
 * <p>当使用SocketChannel写数据之前,先对数据进行加密.
 *
 * <pre>
 *   加密数据后,然后将加密数据通过SocketChannel write方法传输.
 * </pre>
 *
 * @author admin
 */
public class SslSocketChannelOutputStream extends OutputStream {
  SslHandler sslStreams;
  SSLEngine engine;
  int app_buf_size;
  int packet_buf_size;
  ByteBuffer buf;
  boolean closed = false;
  byte single[] = new byte[1];

  public SslSocketChannelOutputStream(SslHandler sslStreams) {
    this.sslStreams = sslStreams;
    this.engine = sslStreams.getSSLEngine();
    buf = allocate(BufType.APPLICATION);
  }

  public void write(int b) throws IOException {
    single[0] = (byte) b;
    write(single, 0, 1);
  }

  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte b[], int off, int len) throws IOException {
    if (closed) {
      throw new IOException("output stream is closed");
    }
    while (len > 0) {
      int l = len > buf.capacity() ? buf.capacity() : len;
      buf.clear();
      buf.put(b, off, l);
      len -= l;
      off += l;
      buf.flip();
      WrapperResult r = sslStreams.sendData(buf);
      if (r.result.getStatus() == Status.CLOSED) {
        closed = true;
        if (len > 0) {
          throw new IOException("output stream is closed");
        }
      }
    }
  }

  public void flush() throws IOException {
    /* no-op */
  }

  public void close() throws IOException {
    WrapperResult r = null;
    engine.closeOutbound();
    closed = true;
    HandshakeStatus stat = HandshakeStatus.NEED_WRAP;
    buf.clear();
    while (stat == HandshakeStatus.NEED_WRAP) {
      r = sslStreams.wrapper.wrapAndSend(buf);
      stat = r.result.getHandshakeStatus();
    }
    assert r.result.getStatus() == Status.CLOSED;
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
