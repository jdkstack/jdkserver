package org.jdkstack.jdkserver.tcp.core.core.client;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jdkstack.jdklog.logging.api.spi.Log;
import org.jdkstack.jdklog.logging.core.factory.LogFactory;
import org.jdkstack.jdkserver.tcp.core.api.core.client.JdkClientChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.core.channel.ChannelException;
import org.jdkstack.jdkserver.tcp.core.core.channel.ClientMode;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.handler.AbstractChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.core.handler.DefaultChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.SslHandler;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelInputStream;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelOutputStream;

public class JdkClientSocketChannel extends AbstractJdkChannel implements JdkClientChannel {
  /** . */
  private static final Log LOG = LogFactory.getLog(JdkClientSocketChannel.class);

  private final SelectorProvider provider = SelectorProvider.provider();
  private final SocketChannel socketChannel = this.socketChannel();
  private final Selector selector = this.openSelector();
  protected final SelectionKey selectionKey = this.register();
  private final Socket socket = this.socketChannel.socket();
  private AbstractChannelHandlerContext ctx = new DefaultChannelHandlerContext(socketChannel);
  private NetworkMessageToByteEncoderHandler encoder;
  private NetworkByteToMessageDecoderHandler decoder;
  private Handler<JdkClientSocketChannel> handler;

  private ClientChannelHandler clientChannelHandler;

  private SslHandler sslStreams;

  private InputStream inputStream;
  private OutputStream outputStream;
  private OutputStream tmpout;
  private Handler<JdkClientSocketChannel> handlerRead;
  private Handler<JdkClientSocketChannel> handlerReadSsl;

  private Handler<JdkClientSocketChannel> handlerWrite;
  private Handler<JdkClientSocketChannel> handlerWriteSsl;

  public void setHandlerWrite(Handler<JdkClientSocketChannel> handlerWrite) {
    this.handlerWrite = handlerWrite;
  }

  public void setHandlerWriteSsl(Handler<JdkClientSocketChannel> handlerWriteSsl) {
    this.handlerWriteSsl = handlerWriteSsl;
  }

  public void setHandlerReadSsl(Handler<JdkClientSocketChannel> handlerReadSsl) {
    this.handlerReadSsl = handlerReadSsl;
  }

  public void setHandlerRead(Handler<JdkClientSocketChannel> handlerRead) {
    this.handlerRead = handlerRead;
  }

  public void setClientChannelHandler(ClientChannelHandler clientChannelHandler) {
    this.clientChannelHandler = clientChannelHandler;
  }

  public void setEncoder(NetworkMessageToByteEncoderHandler encoder) {
    this.encoder = encoder;
  }

  public void setDecoder(NetworkByteToMessageDecoderHandler decoder) {
    this.decoder = decoder;
  }

  public void setHandler(Handler<JdkClientSocketChannel> handler) {
    this.handler = handler;
  }

  public JdkClientSocketChannel() {
    // tcp 不延迟.
    try {
      this.socket.setTcpNoDelay(true);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  public final Selector openSelector() {
    try {
      return this.provider.openSelector();
    } catch (Exception e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  public final SelectionKey register() {
    try {
      return this.socketChannel.register(this.selector, 0, this);
    } catch (Exception e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  public boolean connect(final SocketAddress remoteAddress) throws Exception {
    boolean success = false;
    try {
      final boolean connected = this.socketChannel.connect(remoteAddress);
      if (!connected) {
        this.selectionKey.interestOps(SelectionKey.OP_CONNECT);
      }
      success = true;
      // handler.handle(this);
      return connected;
    } finally {
      if (!success) {
        this.close();
      }
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

  @Override
  public void connectEvent() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_CONNECT) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_CONNECT);
      }
    }
  }

  public boolean connect(final SocketAddress remoteAddress, final SocketAddress localAddress)
      throws Exception {
    // 绑定客户端IP.
    this.socketChannel.bind(localAddress);
    boolean success = false;
    try {
      // 连接到服务端IP.
      final boolean connected = this.socketChannel.connect(remoteAddress);
      success = true;
      // handler.handle(this);
      return connected;
    } finally {
      if (!success) {
        this.close();
      }
    }
  }

  @Override
  public void readEvent() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_ACCEPT) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_ACCEPT);
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
    // LOG.error("客户接收到的数据:{}", s);
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
    while (true) {
      // 创建ByteBuffer，并开辟一个1k的缓冲区.
      ByteBuffer buffer = ByteBuffer.allocate(4);
      // 将通道的数据读取到缓冲区，read方法返回读取到的字节数.
      int messageHeader = socketChannel.read(buffer);
      if (messageHeader > 0) {
        buffer.flip();
        // message Length.
        int messageLength = buffer.getInt();
        ByteBuffer messageBody = ByteBuffer.allocate(messageLength - 4);
        //
        int messageBodyLength = socketChannel.read(messageBody);
        if (messageBodyLength > 0) {
          messageBody.flip();
          decoder.read(ctx, messageBody);
        }
      } else {
        break;
      }
    }
    /*    // 创建ByteBuffer，并开辟一个4字节的的缓冲区.
    ByteBuffer buffer = ByteBuffer.allocate(Constants.LENGTH);
    // 将通道的数据读取到缓冲区,read方法返回读取到的字节数.
    int readLengthBytes = socketChannel.read(buffer);
    // 如果读到了4个字节.
    if (readLengthBytes > 0) {
      // 将Buffer从写模式切换到读模式. read内部是向缓冲写数据.
      buffer.flip();
      // 得到整个单行报文的长度.
      int length = buffer.getInt();
      // length - 4 = 实际的报文长度.
      ByteBuffer body = ByteBuffer.allocate(length - Constants.LENGTH);
      // 将通道的数据读取到缓冲区,read方法返回读取到的字节数.
      int readBodyBytes = socketChannel.read(body);
      // 如果读取到了报文.
      if (readBodyBytes > 0) {
        // 将Buffer从写模式切换到读模式. read内部是向缓冲写数据.
        body.flip();
        // 对读取到的报文进行解码.
        decoder.read(ctx, body);
      }
    }*/
  }

  public final SocketChannel socketChannel() {
    try {
      SocketChannel socketChannel = provider.openSocketChannel();
      socketChannel.configureBlocking(false);
      // socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1000000);
      // socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1000000);
      return socketChannel;
    } catch (IOException e) {
      throw new ChannelException("Failed to open a socket.", e);
    }
  }

  @Override
  public void close() {
    try {
      if (this.socketChannel != null) {
        this.socketChannel.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write2(Handler<ByteBuffer> handler) {
    //
    ctx.setWriteHandler2(handler);
  }

  @Override
  public void write2(ByteBuffer msg) {
    try {
      int attemptedBytes = msg.remaining();
      final int localWrittenBytes = socketChannel.write(msg);
      if (localWrittenBytes > 0) {
        // LOG.error("客户端发送的数据:{}", localWrittenBytes);
        /*if (!selectionKey.isValid()) {
          return;
        }
        final int interestOps = selectionKey.interestOps();
        if ((interestOps & SelectionKey.OP_WRITE) != 0) {
          selectionKey.interestOps(interestOps & ~SelectionKey.OP_WRITE);
        }*/
      }

      if (localWrittenBytes <= 0) {
        //
        ctx.getChannelOutboundBuffer2().enqueue(msg);
      }
    } catch (IOException e) {
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
      int attemptedBytes = msg.remaining();
      final int localWrittenBytes = socketChannel.write(msg);
      if (localWrittenBytes <= 0) {
        ctx.getChannelOutboundBuffer2().enqueue(msg);
        /*    if (!selectionKey.isValid()) {
          return;
        }
        final int interestOps = selectionKey.interestOps();
        if ((interestOps & SelectionKey.OP_WRITE) == 0) {
          selectionKey.interestOps(interestOps | SelectionKey.OP_WRITE);
        }*/
      } else {
        // LOG.error("客户端发送的数据:{}", localWrittenBytes);
      }
    } catch (IOException e) {
      e.printStackTrace();
      try {
        socketChannel.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
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
    if (socketChannel.isConnectionPending()) {
      this.socketChannel.finishConnect();
    }
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(
          createKeyManagers("conf\\client.jks", "storepass", "keypass"),
          createTrustManagers("conf\\trustedCerts.jks", "storepass"),
          new SecureRandom());
      sslStreams = new SslHandler(context, socketChannel, ClientMode.CLIENT);
      inputStream = new SslSocketChannelInputStream(sslStreams);
      outputStream = new SslSocketChannelOutputStream(sslStreams);
      tmpout = new BufferedOutputStream(outputStream);
      if (handler != null) {
        handler.handle(this);
      }
      if (handlerWrite != null) {
        handlerWrite.handle(this);
      }
      if (handlerWriteSsl != null) {
        handlerWriteSsl.handle(this);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  public int select(int ms) throws IOException {
    return selector.select(ms);
  }

  public Set<SelectionKey> selectedKeys() {
    return selector.selectedKeys();
  }

  /*  public SocketChannel accept() {
    try {
      return SocketUtils.accept(serverSocketChannel);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }*/

  public Selector selector() {
    return this.selector;
  }

  public SelectionKey selectionKey() {
    return this.selectionKey;
  }

  public SocketChannel getSocketChannel() {
    return socketChannel;
  }

  public void readEventDown() {
    if (this.selectionKey.isValid()) {
      int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_READ) != 0) {
        this.selectionKey.interestOps(interestOps & ~SelectionKey.OP_READ);
      }
    }
  }

  @Override
  public void readEventUp() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_READ) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_READ);
      }
    }
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_WRITE) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_WRITE);
      }
    }
  }
}
