package org.jdkstack.jdkserver.tcp.core.tcp.server;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import org.jdkstack.jdkserver.tcp.core.JdkServerSocketChannelMessageHandler;
import org.jdkstack.jdkserver.tcp.core.channel.JdkServerChannel;
import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeChannel;
import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeSocketChannel;

public class JdkServerSocketChannelWorker implements Runnable {
  private final JdkServerSocketChannel serverSocketChannel;
  private JdkServerSocketChannelMessageHandler messageHandler;
  private volatile boolean isRun;

  public JdkServerSocketChannelWorker(
      JdkServerSocketChannel serverSocketChannel,
      JdkServerSocketChannelMessageHandler messageHandler) {
    this.serverSocketChannel = serverSocketChannel;
    this.messageHandler = messageHandler;
  }

  @Override
  public void run() {
    while (!isRun) {
      SelectionKey key = messageHandler.poll();
      if (key == null) {
        continue;
      }
      // 事件绑定的channel对象(事件绑定三种channel:JdkServerChannel->JdkBridgeChannel<-JdkClientChannel)
      final Object channel = key.attachment();
      // 获取事件的常量,16表示OP_ACCEPT事件,1表示OP_READ事件.
      final int readyOps = key.readyOps();
      if (channel instanceof JdkServerChannel) {
        JdkServerChannel jdkServerChannel = (JdkServerChannel) channel;
        // 处理服务器端channel事件,主要是OP_ACCEPT事件.
        handlerJdkServerChannel(key, readyOps, jdkServerChannel);
      }
      if (channel instanceof JdkBridgeChannel) {
        JdkBridgeChannel jdkBridgeChannel = (JdkBridgeChannel) channel;
        // 处理服务器端和客户端之间的中间桥梁channel事件,主要是OP_READ和OP_WRITE事件.
        handlerJdkBridgeChannel(key, readyOps, jdkBridgeChannel);
      }
    }
  }

  public void handlerJdkBridgeChannel(
      SelectionKey key, int readyOps, JdkBridgeChannel jdkBridgeChannel) {
    // 如果key是有效的.
    if (key.isValid()) {
      if ((readyOps & SelectionKey.OP_WRITE) != 0 || readyOps == 0) {
        try {
          jdkBridgeChannel.write(null);
        } catch (final Exception e) {
          e.printStackTrace();
          jdkBridgeChannel.close();
        }
      }

      if ((readyOps & (SelectionKey.OP_READ)) != 0 || readyOps == 0) {
        try {
          jdkBridgeChannel.read();
        } catch (final Exception e) {
          e.printStackTrace();
          jdkBridgeChannel.close();
        }
      }
    }
  }

  public void handlerJdkServerChannel(
      SelectionKey key, int readyOps, JdkServerChannel jdkServerChannel) {
    // 如果key是有效的.
    if (key.isValid()) {
      if ((readyOps & (SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
        JdkBridgeChannel jdkBridgeChannel =
            new JdkBridgeSocketChannel(
                serverSocketChannel.getServerSocketChannel(), serverSocketChannel.selector());
        try {
          jdkBridgeChannel.readEventUp();
        } catch (final Exception e) {
          e.printStackTrace();
          jdkBridgeChannel.close();
        }
      }
    }
  }

  public final void bind(final SocketAddress localAddress, final int backlog) throws Exception {
    this.serverSocketChannel.bind(localAddress, backlog);
  }

  public void acceptEvent() {
    this.serverSocketChannel.acceptEvent();
  }

  public void readEvent() {
    this.serverSocketChannel.readEventUp();
  }

  public synchronized boolean isRun() {
    return isRun;
  }

  public synchronized void setRun(boolean run) {
    isRun = run;
  }
}
