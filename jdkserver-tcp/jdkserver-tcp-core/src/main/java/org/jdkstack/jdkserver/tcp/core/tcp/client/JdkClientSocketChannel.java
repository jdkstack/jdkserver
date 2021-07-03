package org.jdkstack.jdkserver.tcp.core.tcp.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import org.jdkstack.jdkserver.tcp.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.channel.ChannelException;
import org.jdkstack.jdkserver.tcp.core.channel.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.channel.ClientChannelHandler;
import org.jdkstack.jdkserver.tcp.core.channel.ClientChannelReadWriteHandler;
import org.jdkstack.jdkserver.tcp.core.channel.DefaultChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.channel.JdkClientChannel;
import org.jdkstack.jdkserver.tcp.core.channel.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.channel.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.common.SocketUtils;
import org.study.core.future.Handler;
import org.study.network.codecs.Message;

public class JdkClientSocketChannel extends AbstractJdkChannel implements JdkClientChannel {
  private final SocketChannel socketChannel = this.socketChannel();
  private final SelectorProvider provider = SelectorProvider.provider();
  private final Selector selector = this.openSelector();
  protected final SelectionKey selectionKey = this.register();
  private final Socket socket = this.socketChannel.socket();
  private final NetworkMessageToByteEncoderHandler encoder =
      new NetworkMessageToByteEncoderHandler();
  private final NetworkByteToMessageDecoderHandler decoder =
      new NetworkByteToMessageDecoderHandler();
  private final ClientChannelReadWriteHandler handler = new ClientChannelReadWriteHandler();
  private final ClientChannelHandler clientChannelHandler = new ClientChannelHandler(handler);
  protected final ChannelHandlerContext ctx =
      new DefaultChannelHandlerContext(socketChannel, clientChannelHandler);

  public JdkClientSocketChannel() {
    clientChannelHandler.setChannel(this);
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
      final boolean connected = SocketUtils.connect(this.socketChannel, remoteAddress);
      if (!connected) {
        this.selectionKey.interestOps(SelectionKey.OP_CONNECT);
      }
      success = true;
      return connected;
    } finally {
      if (!success) {
        this.close();
      }
    }
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
    SocketUtils.bind(this.socketChannel, localAddress);
    boolean success = false;
    try {
      final boolean connected = SocketUtils.connect(this.socketChannel, remoteAddress);
      success = true;
      return connected;
    } finally {
      if (!success) {
        this.close();
      }
    }
  }

  public void init() {
    try {
      // 将服务器端的channel设置成非阻塞.
      this.socketChannel.configureBlocking(false);
    } catch (IOException e) {
      e.printStackTrace();
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

  public void write(Handler<Message> handler) {
    //
    ctx.setWriteHandler(handler);
  }

  public void read(Handler<Message> handler) {
    //
    ctx.setHandler(handler);
  }

  @Override
  public void read() throws Exception {
    /*创建ByteBuffer，并开辟一个1k的缓冲区*/
    ByteBuffer buffer = ByteBuffer.allocate(4);
    /*将通道的数据读取到缓冲区，read方法返回读取到的字节数*/
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

  public final SocketChannel socketChannel() {
    try {
      SocketChannel socketChannel = DEFAULT_SELECTOR_PROVIDER.openSocketChannel();
      socketChannel.configureBlocking(false);
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
    this.socketChannel.finishConnect();
  }

  public int getReceiveBufferSize() {
    try {
      return socket.getReceiveBufferSize();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setReceiveBufferSize(int receiveBufferSize) {
    try {
      socket.setReceiveBufferSize(receiveBufferSize);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public int getSendBufferSize() {
    try {
      return socket.getSendBufferSize();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setSendBufferSize(int sendBufferSize) {
    try {
      socket.setSendBufferSize(sendBufferSize);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public int getSoLinger() {
    try {
      return socket.getSoLinger();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
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
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public int getTrafficClass() {
    try {
      return socket.getTrafficClass();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setTrafficClass(int trafficClass) {
    try {
      socket.setTrafficClass(trafficClass);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public boolean isKeepAlive() {
    try {
      return socket.getKeepAlive();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setKeepAlive(boolean keepAlive) {
    try {
      socket.setKeepAlive(keepAlive);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public boolean isReuseAddress() {
    try {
      return socket.getReuseAddress();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setReuseAddress(boolean reuseAddress) {
    try {
      socket.setReuseAddress(reuseAddress);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public boolean isTcpNoDelay() {
    try {
      return socket.getTcpNoDelay();
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
    }
  }

  public void setTcpNoDelay(boolean tcpNoDelay) {
    try {
      socket.setTcpNoDelay(tcpNoDelay);
    } catch (SocketException e) {
      throw new io.netty.channel.ChannelException(e);
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
  }
}
