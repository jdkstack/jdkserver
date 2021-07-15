package org.jdkstack.jdkserver.tcp.core.ssl.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.jdkstack.jdkserver.tcp.core.core.channel.ClientMode;

/**
 * SSL握手处理.
 *
 * <p>在通过SocketChannel读写数据之前,首先进行SSL握手.
 *
 * <pre>
 *   SSL握手成功后,对写数据进行加密和对读数据进行解密操作.
 * </pre>
 *
 * @author admin
 */
public class SslHandler {
  SSLContext sslctx;
  SocketChannel chan;
  SSLEngine engine;
  public EngineWrapper wrapper;
  OutputStream os;
  InputStream is;

  /* held by thread doing the hand-shake on this connection */
  Lock handshaking = new ReentrantLock();
  int app_buf_size;
  int packet_buf_size;

  public SslHandler(SSLContext sslctx, SocketChannel chan, ClientMode clientMode) {
    this.sslctx = sslctx;
    this.chan = chan;
    // 远程对等点的Socket.
    Socket socket = chan.socket();
    // 远程对等点的SocketAddress.
    SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
    // 远程对等点的InetSocketAddress.
    InetSocketAddress addr = (InetSocketAddress) remoteSocketAddress;
    // 远程对等点的IP/HOSTNAME(相对于服务端,对等点是客户端,相对于客户端对等点是服务端).
    final String hostName = addr.getHostName();
    // 远程对等点的PORT(相对于服务端,对等点是客户端,相对于客户端对等点是服务端).
    final int port = addr.getPort();
    // 创建engine,并绑定远程对等点的IP/HOSTNAME和PORT.
    engine = sslctx.createSSLEngine(hostName, port);
    switch (clientMode) {
        // 如果是服务端模式.
      case SERVER:
        // 设置engine为服务端模式.
        engine.setUseClientMode(false);
        // 客户端必须提供证书,不携带直接close.
        engine.setNeedClientAuth(true);
        break;
        // 如果是客户端模式.
      case CLIENT:
        // 设置engine为客户端模式.
        engine.setUseClientMode(true);
        break;
      default:
        break;
    }
    wrapper = new EngineWrapper(chan, engine);
  }

  /** cleanup resources allocated inside this object */
  void close() throws IOException {
    wrapper.close();
  }

  public SSLEngine getSSLEngine() {
    return engine;
  }

  /**
   * request the engine to repeat the handshake on this session the handshake must be driven by
   * reads/writes on the streams Normally, not necessary to call this.
   */
  void beginHandshake() throws SSLException {
    engine.beginHandshake();
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

  /**
   * send the data in the given ByteBuffer. If a handshake is needed then this is handled within
   * this method. When this call returns, all of the given user data has been sent and any handshake
   * has been completed. Caller should check if engine has been closed.
   */
  public WrapperResult sendData(ByteBuffer src) throws IOException {
    WrapperResult r = null;
    while (src.remaining() > 0) {
      r = wrapper.wrapAndSend(src);
      Status status = r.result.getStatus();
      if (status == Status.CLOSED) {
        doClosure();
        return r;
      }
      HandshakeStatus hs_status = r.result.getHandshakeStatus();
      if (hs_status != HandshakeStatus.FINISHED && hs_status != HandshakeStatus.NOT_HANDSHAKING) {
        doHandshake(hs_status);
      }
    }
    return r;
  }

  /**
   * read data thru the engine into the given ByteBuffer. If the given buffer was not large enough,
   * a new one is allocated and returned. This call handles handshaking automatically. Caller should
   * check if engine has been closed.
   */
  public WrapperResult recvData(ByteBuffer dst) throws IOException {
    /* we wait until some user data arrives */
    WrapperResult r = null;
    assert dst.position() == 0;
    while (dst.position() == 0) {
      r = wrapper.recvAndUnwrap(dst);
      dst = (r.buf != dst) ? r.buf : dst;
      Status status = r.result.getStatus();
      if (status == Status.CLOSED) {
        doClosure();
        return r;
      }

      HandshakeStatus hs_status = r.result.getHandshakeStatus();
      if (hs_status != HandshakeStatus.FINISHED && hs_status != HandshakeStatus.NOT_HANDSHAKING) {
        doHandshake(hs_status);
      }
    }
    dst.flip();
    return r;
  }

  /* we've received a close notify. Need to call wrap to send
   * the response
   */
  public void doClosure() throws IOException {
    try {
      handshaking.lock();
      ByteBuffer tmp = allocate(BufType.APPLICATION);
      WrapperResult r;
      do {
        tmp.clear();
        tmp.flip();
        r = wrapper.wrapAndSendX(tmp, true);
      } while (r.result.getStatus() != Status.CLOSED);
    } finally {
      handshaking.unlock();
    }
  }

  /* do the (complete) handshake after acquiring the handshake lock.
   * If two threads call this at the same time, then we depend
   * on the wrapper methods being idempotent. eg. if wrapAndSend()
   * is called with no data to send then there must be no problem
   */
  public void doHandshake(HandshakeStatus hs_status) throws IOException {
    try {
      handshaking.lock();
      ByteBuffer tmp = allocate(BufType.APPLICATION);
      while (hs_status != HandshakeStatus.FINISHED
          && hs_status != HandshakeStatus.NOT_HANDSHAKING) {
        WrapperResult r = null;
        switch (hs_status) {
          case NEED_TASK:
            Runnable task;
            while ((task = engine.getDelegatedTask()) != null) {
              /* run in current thread, because we are already
               * running an external Executor
               */
              task.run();
            }
            /* fall thru - call wrap again */
          case NEED_WRAP:
            tmp.clear();
            tmp.flip();
            r = wrapper.wrapAndSend(tmp);
            break;

          case NEED_UNWRAP:
            tmp.clear();
            r = wrapper.recvAndUnwrap(tmp);
            if (r.buf != tmp) {
              tmp = r.buf;
            }
            assert tmp.position() == 0;
            break;
        }
        hs_status = r.result.getHandshakeStatus();
      }
    } finally {
      handshaking.unlock();
    }
  }
}
