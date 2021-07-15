package org.jdkstack.jdkserver.tcp.core.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.ChannelConfig;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.api.core.server.JdkServerChannel;
import org.jdkstack.jdkserver.tcp.core.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.core.channel.ChannelException;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessageToByteEncoderHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.BridgeChannelHandler;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.JdkBridgeSocketChannel;

public class JdkServerSocketChannel extends AbstractJdkChannel implements JdkServerChannel {
  private final SelectorProvider provider = SelectorProvider.provider();
  private final ServerSocketChannel serverSocketChannel = this.serverSocketChannel();
  private final ServerSocket serverSocket = this.serverSocketChannel.socket();
  private final Selector selector = this.openSelector();
  protected final SelectionKey selectionKey = this.register();
  private ChannelConfig config;

  private NetworkMessageToByteEncoderHandler encoder;
  private NetworkByteToMessageDecoderHandler decoder;
  private Handler<JdkBridgeSocketChannel> handler;
  private BridgeChannelHandler bridgeChannelHandler;

  private Handler<JdkBridgeSocketChannel> handlerRead;
  private Handler<JdkBridgeSocketChannel> handlerReadSsl;

  private Handler<JdkBridgeSocketChannel> handlerWrite;
  private Handler<JdkBridgeSocketChannel> handlerWriteSsl;

  public void setConfig(ChannelConfig config) {
    this.config = config;
  }

  public void setEncoder(NetworkMessageToByteEncoderHandler encoder) {
    this.encoder = encoder;
  }

  public void setDecoder(NetworkByteToMessageDecoderHandler decoder) {
    this.decoder = decoder;
  }

  public void setHandler(Handler<JdkBridgeSocketChannel> handler) {
    this.handler = handler;
  }

  public void setBridgeChannelHandler(BridgeChannelHandler bridgeChannelHandler) {
    this.bridgeChannelHandler = bridgeChannelHandler;
  }

  public Handler<JdkBridgeSocketChannel> getHandlerRead() {
    return handlerRead;
  }

  public void setHandlerRead(Handler<JdkBridgeSocketChannel> handlerRead) {
    this.handlerRead = handlerRead;
  }

  public Handler<JdkBridgeSocketChannel> getHandlerReadSsl() {
    return handlerReadSsl;
  }

  public void setHandlerReadSsl(Handler<JdkBridgeSocketChannel> handlerReadSsl) {
    this.handlerReadSsl = handlerReadSsl;
  }

  public Handler<JdkBridgeSocketChannel> getHandlerWrite() {
    return handlerWrite;
  }

  public void setHandlerWrite(Handler<JdkBridgeSocketChannel> handlerWrite) {
    this.handlerWrite = handlerWrite;
  }

  public Handler<JdkBridgeSocketChannel> getHandlerWriteSsl() {
    return handlerWriteSsl;
  }

  public void setHandlerWriteSsl(Handler<JdkBridgeSocketChannel> handlerWriteSsl) {
    this.handlerWriteSsl = handlerWriteSsl;
  }

  public NetworkMessageToByteEncoderHandler getEncoder() {
    return encoder;
  }

  public NetworkByteToMessageDecoderHandler getDecoder() {
    return decoder;
  }

  public Handler<JdkBridgeSocketChannel> getHandler() {
    return handler;
  }

  public BridgeChannelHandler getBridgeChannelHandler() {
    return bridgeChannelHandler;
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
      return this.serverSocketChannel.register(this.selector, 0, this); // SelectionKey.OP_ACCEPT
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  public final ServerSocketChannel serverSocketChannel() {
    try {
      ServerSocketChannel serverSocketChannel = provider.openServerSocketChannel();
      serverSocketChannel.configureBlocking(false);
      return serverSocketChannel;
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    }
  }

  @Override
  public final void bind(final SocketAddress localAddress, final int backlog) throws Exception {
    ServerSocket serverSocket = serverSocketChannel.socket();
    serverSocket.bind(localAddress, backlog);
  }

  @Override
  public final SelectionKey register(final Selector selector, final int ops) {
    try {
      return this.serverSocketChannel.register(selector, ops);
    } catch (ClosedChannelException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void init() {
    try {
      // 将服务器端的channel设置成非阻塞.
      this.serverSocketChannel.configureBlocking(false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void acceptEvent() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_ACCEPT) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_ACCEPT);
      }
    }
  }

  @Override
  public void accept() {
    //
  }

  @Override
  public void acceptEventDown() {
    if (this.selectionKey.isValid()) {
      int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_ACCEPT) != 0) {
        this.selectionKey.interestOps(interestOps & ~SelectionKey.OP_ACCEPT);
      }
    }
  }

  @Override
  public void acceptEventUp() {
    if (this.selectionKey.isValid()) {
      final int interestOps = this.selectionKey.interestOps();
      if ((interestOps & SelectionKey.OP_ACCEPT) == 0) {
        this.selectionKey.interestOps(interestOps | SelectionKey.OP_ACCEPT);
      }
    }
  }

  public void close() {
    try {
      if (this.serverSocketChannel != null) {
        this.serverSocketChannel.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isReuseAddress() {
    try {
      return serverSocket.getReuseAddress();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setReuseAddress(boolean reuseAddress) {
    try {
      serverSocket.setReuseAddress(reuseAddress);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public int getReceiveBufferSize() {
    try {
      return serverSocket.getReceiveBufferSize();
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setReceiveBufferSize(int receiveBufferSize) {
    try {
      serverSocket.setReceiveBufferSize(receiveBufferSize);
    } catch (SocketException e) {
      throw new ChannelException(e);
    }
  }

  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    serverSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
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

  public ServerSocketChannel getServerSocketChannel() {
    return serverSocketChannel;
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
