package org.jdkstack.jdkserver.tcp.core.ssl.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;

public class EngineWrapper {
  SocketChannel chan;
  SSLEngine engine;
  Object wrapLock, unwrapLock;
  ByteBuffer unwrap_src, wrap_dst;
  boolean closed = false;
  int u_remaining; // the number of bytes left in unwrap_src after an unwrap()
  int app_buf_size;
  int packet_buf_size;
  EngineWrapper(SocketChannel chan, SSLEngine engine) {
    this.chan = chan;
    this.engine = engine;
    wrapLock = new Object();
    unwrapLock = new Object();
    unwrap_src = allocate(BufType.PACKET);
    wrap_dst = allocate(BufType.PACKET);
  }

  void close() throws IOException {}

  /* try to wrap and send the data in src. Handles OVERFLOW.
   * Might block if there is an outbound blockage or if another
   * thread is calling wrap(). Also, might not send any data
   * if an unwrap is needed.
   */
  public  WrapperResult wrapAndSend(ByteBuffer src) throws IOException {
    return wrapAndSendX(src, false);
  }

  public   WrapperResult wrapAndSendX(ByteBuffer src, boolean ignoreClose) throws IOException {
    if (closed && !ignoreClose) {
      throw new IOException("Engine is closed");
    }
    Status status;
    WrapperResult r = new WrapperResult();
    synchronized (wrapLock) {
      wrap_dst.clear();
      do {
        r.result = engine.wrap(src, wrap_dst);
        status = r.result.getStatus();
        if (status == Status.BUFFER_OVERFLOW) {
          wrap_dst = realloc(wrap_dst, true, BufType.PACKET);
        }
      } while (status == Status.BUFFER_OVERFLOW);
      if (status == Status.CLOSED && !ignoreClose) {
        closed = true;
        return r;
      }
      if (r.result.bytesProduced() > 0) {
        wrap_dst.flip();
        int l = wrap_dst.remaining();
        assert l == r.result.bytesProduced();
        while (l > 0) {
          l -= chan.write(wrap_dst);
        }
      }
    }
    return r;
  }

  /* block until a complete message is available and return it
   * in dst, together with the Result. dst may have been re-allocated
   * so caller should check the returned value in Result
   * If handshaking is in progress then, possibly no data is returned
   */
  public  WrapperResult recvAndUnwrap(ByteBuffer dst) throws IOException {
    Status status = Status.OK;
    WrapperResult r = new WrapperResult();
    r.buf = dst;
    if (closed) {
      throw new IOException("Engine is closed");
    }
    boolean needData;
    if (u_remaining > 0) {
      unwrap_src.compact();
      unwrap_src.flip();
      needData = false;
    } else {
      unwrap_src.clear();
      needData = true;
    }
    synchronized (unwrapLock) {
      int x;
      do {
        if (needData) {
          do {
            x = chan.read(unwrap_src);
          } while (x == 0);
          if (x == -1) {
            throw new IOException("connection closed for reading");
          }
          unwrap_src.flip();
        }
        r.result = engine.unwrap(unwrap_src, r.buf);
        status = r.result.getStatus();
        if (status == Status.BUFFER_UNDERFLOW) {
          if (unwrap_src.limit() == unwrap_src.capacity()) {
            /* buffer not big enough */
            unwrap_src = realloc(unwrap_src, false, BufType.PACKET);
          } else {
            /* Buffer not full, just need to read more
             * data off the channel. Reset pointers
             * for reading off SocketChannel
             */
            unwrap_src.position(unwrap_src.limit());
            unwrap_src.limit(unwrap_src.capacity());
          }
          needData = true;
        } else if (status == Status.BUFFER_OVERFLOW) {
          r.buf = realloc(r.buf, true, BufType.APPLICATION);
          needData = false;
        } else if (status == Status.CLOSED) {
          closed = true;
          r.buf.flip();
          return r;
        }
      } while (status != Status.OK);
    }
    u_remaining = unwrap_src.remaining();
    return r;
  }


  private ByteBuffer allocate(BufType type) {
    return allocate(type, -1);
  }

  private ByteBuffer allocate(BufType type, int len) {
    assert engine != null;
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

  /* reallocates the buffer by :-
   * 1. creating a new buffer double the size of the old one
   * 2. putting the contents of the old buffer into the new one
   * 3. set xx_buf_size to the new size if it was smaller than new size
   *
   * flip is set to true if the old buffer needs to be flipped
   * before it is copied.
   */
  private ByteBuffer realloc(ByteBuffer b, boolean flip, BufType type) {
    synchronized (this) {
      int nsize = 2 * b.capacity();
      ByteBuffer n = allocate(type, nsize);
      if (flip) {
        b.flip();
      }
      n.put(b);
      b = n;
    }
    return b;
  }
}
