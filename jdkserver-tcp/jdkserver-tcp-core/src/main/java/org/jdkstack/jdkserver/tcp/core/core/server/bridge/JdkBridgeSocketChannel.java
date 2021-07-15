package org.jdkstack.jdkserver.tcp.core.core.server.bridge;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jdkstack.jdklog.logging.api.spi.Log;
import org.jdkstack.jdklog.logging.core.factory.LogFactory;
import org.jdkstack.jdkserver.tcp.core.api.core.bridge.JdkBridgeChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.core.channel.ChannelException;
import org.jdkstack.jdkserver.tcp.core.core.channel.ClientMode;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.handler.DefaultChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.core.server.JdkServerSocketChannel;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.SslHandler;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelInputStream;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelOutputStream;

public class JdkBridgeSocketChannel extends AbstractJdkChannel implements JdkBridgeChannel {
  /** . */
  private static final Log LOG = LogFactory.getLog(JdkBridgeSocketChannel.class);

  private final SelectorProvider provider = SelectorProvider.provider();
  private SelectionKey selectionKey;
  private SocketChannel socketChannel;
  private Socket socket;
  private Selector selector;
  private ChannelHandlerContext ctx;
  private NetworkMessageToByteEncoderHandler encoder;
  private NetworkByteToMessageDecoderHandler decoder;
  private Handler<JdkBridgeSocketChannel> handler;
  /** SocketChannel状态 isConnected isRegistered isOpen isBlocking isConnectionPending. */
  public int status;

  public Handler<JdkBridgeSocketChannel> getHandler() {
    return handler;
  }

  private BridgeChannelHandler bridgeChannelHandler;

  public BridgeChannelHandler getBridgeChannelHandler() {
    return bridgeChannelHandler;
  }

  private SslHandler sslStreams;
  private InputStream inputStream;
  private OutputStream outputStream;
  private OutputStream tmpout;
  private Handler<JdkBridgeSocketChannel> handlerRead;
  private Handler<JdkBridgeSocketChannel> handlerReadSsl;

  private Handler<JdkBridgeSocketChannel> handlerWrite;
  private Handler<JdkBridgeSocketChannel> handlerWriteSsl;

  public void setHandlerRead(Handler<JdkBridgeSocketChannel> handlerRead) {
    this.handlerRead = handlerRead;
  }

  public void setHandlerReadSsl(Handler<JdkBridgeSocketChannel> handlerReadSsl) {
    this.handlerReadSsl = handlerReadSsl;
  }

  public void setHandlerWrite(Handler<JdkBridgeSocketChannel> handlerWrite) {
    this.handlerWrite = handlerWrite;
  }

  public void setHandlerWriteSsl(Handler<JdkBridgeSocketChannel> handlerWriteSsl) {
    this.handlerWriteSsl = handlerWriteSsl;
  }

  public JdkBridgeSocketChannel(JdkServerSocketChannel jdksocketChannel) {
    try {
      ServerSocketChannel serverSocketChannel = jdksocketChannel.getServerSocketChannel();
      Selector selector = jdksocketChannel.selector();
      SocketChannel acceptSocketChannel = serverSocketChannel.accept();
      acceptSocketChannel.configureBlocking(false);
      acceptSocketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1000000);
      acceptSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1000000);
      this.socketChannel = acceptSocketChannel;
      this.socket = this.socketChannel.socket();
      // tcp 不延迟.
      this.socket.setTcpNoDelay(true);
      this.selector = selector;
      this.selectionKey = this.register();
      this.encoder = jdksocketChannel.getEncoder();
      this.decoder = jdksocketChannel.getDecoder();
      this.handler = jdksocketChannel.getHandler();
      this.bridgeChannelHandler = jdksocketChannel.getBridgeChannelHandler();
      this.handlerRead = jdksocketChannel.getHandlerRead();
      this.handlerReadSsl = jdksocketChannel.getHandlerReadSsl();
      this.handlerWrite = jdksocketChannel.getHandlerWrite();
      this.handlerWriteSsl = jdksocketChannel.getHandlerWriteSsl();
      this.ctx = new DefaultChannelHandlerContext(socketChannel);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(
          createKeyManagers("conf\\client.jks", "storepass", "keypass"),
          createTrustManagers("conf\\trustedCerts.jks", "storepass"),
          new SecureRandom());
      sslStreams = new SslHandler(context, socketChannel, ClientMode.SERVER);
      inputStream = new SslSocketChannelInputStream(sslStreams);
      outputStream = new SslSocketChannelOutputStream(sslStreams);
      tmpout = new BufferedOutputStream(outputStream);
      if (bridgeChannelHandler != null) {
        bridgeChannelHandler.setChannel(this);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as
   * an input.
   *
   * @param filepath - the path to the JKS keystore.
   * @param keystorePassword - the keystore's password.
   * @param keyPassword - the key's passsword.
   * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
   * @throws Exception
   */
  protected static KeyManager[] createKeyManagers(
      String filepath, String keystorePassword, String keyPassword) throws Exception {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    InputStream keyStoreIS = new FileInputStream(filepath);
    try {
      keyStore.load(keyStoreIS, keystorePassword.toCharArray());
    } finally {
      if (keyStoreIS != null) {
        keyStoreIS.close();
      }
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, keyPassword.toCharArray());
    return kmf.getKeyManagers();
  }

  /**
   * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as
   * an input.
   *
   * @param filepath - the path to the JKS keystore.
   * @param keystorePassword - the keystore's password.
   * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
   * @throws Exception
   */
  protected static TrustManager[] createTrustManagers(String filepath, String keystorePassword)
      throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");
    InputStream trustStoreIS = new FileInputStream(filepath);
    try {
      trustStore.load(trustStoreIS, keystorePassword.toCharArray());
    } finally {
      if (trustStoreIS != null) {
        trustStoreIS.close();
      }
    }
    TrustManagerFactory trustFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustFactory.init(trustStore);
    return trustFactory.getTrustManagers();
  }

  public final Selector openSelector() {
    try {

      return this.provider.openSelector();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  public final SelectionKey register() {
    try {
      return this.socketChannel.register(this.selector, 0, this);
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  @Override
  public void close() throws Exception {
    if (this.socketChannel != null) {
      this.socketChannel.close();
    }
    selectionKey.cancel();
  }

  public int getReceiveBufferSize() {
    try {
      return socket.getReceiveBufferSize();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setReceiveBufferSize(int receiveBufferSize) {
    try {
      socket.setReceiveBufferSize(receiveBufferSize);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public int getSendBufferSize() {
    try {
      return socket.getSendBufferSize();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setSendBufferSize(int sendBufferSize) {
    try {
      socket.setSendBufferSize(sendBufferSize);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public int getSoLinger() {
    try {
      return socket.getSoLinger();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setSoLinger(int soLinger) {
    try {
      if (soLinger < 0) {
        socket.setSoLinger(false, 0);
      } else {
        socket.setSoLinger(true, soLinger);
      }
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public int getTrafficClass() {
    try {
      return socket.getTrafficClass();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setTrafficClass(int trafficClass) {
    try {
      socket.setTrafficClass(trafficClass);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public boolean isKeepAlive() {
    try {
      return socket.getKeepAlive();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setKeepAlive(boolean keepAlive) {
    try {
      socket.setKeepAlive(keepAlive);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public boolean isReuseAddress() {
    try {
      return socket.getReuseAddress();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setReuseAddress(boolean reuseAddress) {
    try {
      socket.setReuseAddress(reuseAddress);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public boolean isTcpNoDelay() {
    try {
      return socket.getTcpNoDelay();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setTcpNoDelay(boolean tcpNoDelay) {
    try {
      socket.setTcpNoDelay(tcpNoDelay);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    socket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }

  @Override
  public void readEventUp() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_READ) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_READ);
      }
    }
    /* if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_WRITE) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_WRITE);
      }
    }*/
  }

  @Override
  public void readEventDown() {
    if (this.selectionKey.isValid()) {
      int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_READ) != 0) {
        this.selectionKey.interestOps(interestOps & ~SelectionKey.OP_READ);
      }
    }
  }

  public void write(Handler<ByteBuffer> handler) {
    //
    ctx.setWriteHandler(handler);
  }

  public void read(Handler<Message> handler) {
    //
    ctx.setReadHandler(handler);
  }

  @Override
  public void readSsl() throws Exception {
    byte[] b = new byte[1024];
    inputStream.read(b);
    String s = new String(b, StandardCharsets.UTF_8);
    LOG.error("服务端接收到的数据:{}", s);
    System.out.println(s);
  }

  @Override
  public void readHandler() throws Exception {
    if (handlerRead != null) {
      handlerRead.handle(this);
    }
    if (handlerReadSsl != null) {
      handlerReadSsl.handle(this);
    }
  }

  @Override
  public void read() throws Exception {
    // 创建ByteBuffer，并开辟一个1k的缓冲区.
    ByteBuffer buffer = ByteBuffer.allocate(4);
    // 将通道的数据读取到缓冲区，read方法返回读取到的字节数.
    int readBytes = socketChannel.read(buffer);
    if (readBytes > 0) {
      buffer.flip();
      int anInt = buffer.getInt();
      ByteBuffer body = ByteBuffer.allocate(anInt - 4);
      int readBytes1 = socketChannel.read(body);
      if (readBytes1 > 0) {
        body.flip();
        decoder.read(ctx, body);
      }
    }
  }

  public void writeEventDown() {
    if (this.selectionKey.isValid()) {
      int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_WRITE) != 0) {
        this.selectionKey.interestOps(interestOps & ~SelectionKey.OP_WRITE);
      }
    }
  }

  public void writeEventUp() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_WRITE) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_WRITE);
      }
    }
  }

  @Override
  public void write3(Message msg) {
    try {
      ctx.handleWrite2(null);
    } catch (Exception e) {
      e.printStackTrace();
      try {
        socketChannel.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }

  @Override
  public void write1(ByteBuffer msg) {
    try {
      int write = socketChannel.write(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeSsl(Message msg) {
    try {
      tmpout.write(msg.getBody().getBytes(StandardCharsets.UTF_8));
      tmpout.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void write(Message msg) {
    try {
      encoder.write(ctx, msg);
    } catch (Exception e) {
      e.printStackTrace();
      try {
        socketChannel.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }
  }

  @Override
  public void finishConnect() throws IOException {
    if (handler != null) {
      handler.handle(this);
    }
    if (handlerWrite != null) {
      handlerWrite.handle(this);
    }
    if (handlerWriteSsl != null) {
      handlerWriteSsl.handle(this);
    }
  }

  @Override
  public void shutdown() {
    try {
      // sslStreams.getSSLEngine().closeOutbound();
      // sslStreams.doClosure();
      close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void configureBlocking(boolean b) {
    try {
      this.socketChannel.configureBlocking(b);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
