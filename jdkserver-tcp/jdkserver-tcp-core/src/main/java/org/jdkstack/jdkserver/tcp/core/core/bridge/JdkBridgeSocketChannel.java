package org.jdkstack.jdkserver.tcp.core.core.bridge;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import org.jdkstack.jdkserver.tcp.core.common.SocketUtils;
import org.jdkstack.jdkserver.tcp.core.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.core.channel.ChannelException;
import org.jdkstack.jdkserver.tcp.core.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.core.handler.DefaultChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.core.server.ServerChannelHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.ServerChannelReadWriteHandler;
import org.jdkstack.jdkserver.tcp.core.future.Handler;

public class JdkBridgeSocketChannel extends AbstractJdkChannel implements JdkBridgeChannel {
  private final SelectorProvider provider = SelectorProvider.provider();
  private final NetworkMessageToByteEncoderHandler encoder =
      new NetworkMessageToByteEncoderHandler();
  private final NetworkByteToMessageDecoderHandler decoder =
      new NetworkByteToMessageDecoderHandler();
  private final ServerChannelReadWriteHandler handler = new ServerChannelReadWriteHandler();
  private final ServerChannelHandler serverChannelHandler = new ServerChannelHandler(handler);
  protected SelectionKey selectionKey;
  protected ChannelHandlerContext ctx;
  private SocketChannel socketChannel;
  private Socket socket;
  private Selector selector;

  public JdkBridgeSocketChannel(ServerSocketChannel serverSocketChannel, Selector selector) {
    try {
      SocketChannel acceptSocketChannel = SocketUtils.accept(serverSocketChannel);
      acceptSocketChannel.configureBlocking(false);
      this.socketChannel = acceptSocketChannel;
      this.socket = this.socketChannel.socket();
      this.selector = selector;
      this.selectionKey = this.register();
      this.ctx = new DefaultChannelHandlerContext(socketChannel, serverChannelHandler);
      serverChannelHandler.setChannel(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public void close() {
    try {
      if (this.socketChannel != null) {
        this.socketChannel.close();
      }
      selectionKey.cancel();
    } catch (IOException e) {
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

  @Override
  public void readEventUp() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_READ) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_READ);
      }
    }
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
      // System.out.println("读取了多少字节???" + anInt);
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
}
