package org.jdkstack.jdkserver.tcp.core.core.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import org.jdkstack.jdkserver.tcp.core.api.core.bridge.JdkBridgeChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.server.JdkServerChannel;
import org.jdkstack.jdkserver.tcp.core.core.server.bridge.JdkBridgeSocketChannel;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class JdkServerSocketChannelEventRunnable implements Runnable {
  public JdkServerSocketChannel serverSocketChannel;
  public volatile boolean isRun;

  public JdkServerSocketChannelEventRunnable(JdkServerSocketChannel serverSocketChannel) {
    this.serverSocketChannel = serverSocketChannel;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public final void run() {
    while (!isRun) {
      // 获取当前服务器端channel接收到的的客户端事件.
      int events = 0;
      try {
        // 阻塞500ms.
        events = this.serverSocketChannel.select(500);
      } catch (final IOException e) {
        e.printStackTrace();
      }
      // 事件大于0.
      if (0 >= events) {
        continue;
      }
      // 获取服务器端接收到的事件key的集合.
      final Set<SelectionKey> keys = this.serverSocketChannel.selectedKeys();
      // 获取集合的迭代对象.
      final Iterator<SelectionKey> it = keys.iterator();
      // 是否存在一个事件.
      while (it.hasNext()) {
        try {
          // 获取一个事件.
          final SelectionKey key = it.next();
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
          // 获取每一个事件,放入到队列中.
          // messageHandler.enqueue(key);
        } finally {
          // ???
          // 注意, 在每次迭代时, 我们都调用 "it.remove()" 将这个 key 从迭代器中删除,
          // 因为 select() 方法仅仅是简单地将就绪的 IO 操作放到 selectedKeys 集合中,
          // 因此如果我们从 selectedKeys 获取到一个 key, 但是没有将它删除, 那么下一次 select 时, 这个 key 所对应的 IO 事件还在
          // selectedKeys 中.
          it.remove();
        }
      }
      // 清空当前的集合.
      keys.clear();
    }
  }

  public void handlerJdkBridgeChannel(
      SelectionKey key, int readyOps, JdkBridgeChannel jdkBridgeChannel) {
    // 如果key是有效的.
    if (key.isValid()) {
      // 监听写事件(向客户端写).
      if ((readyOps & SelectionKey.OP_WRITE) != 0 || readyOps == 0) {
        try {
          jdkBridgeChannel.write(null);
        } catch (final Exception e) {
          e.printStackTrace();
          jdkBridgeChannel.close();
        }
      }
      // 监听读事件(从客户端读).
      if ((readyOps & SelectionKey.OP_READ) != 0 || readyOps == 0) {
        try {
          // 处理读事件.
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
      // 监听连接事件(接收客户端连接请求).
      if ((readyOps & SelectionKey.OP_ACCEPT) != 0 || readyOps == 0) {
        JdkBridgeChannel jdkBridgeChannel = null;
        try {
          // 处理客户端连接.
          jdkBridgeChannel =
              new JdkBridgeSocketChannel(
                  serverSocketChannel.getServerSocketChannel(), serverSocketChannel.selector());
          // 注册服务端读事件.
          jdkBridgeChannel.readEventUp();
        } catch (final Exception e) {
          e.printStackTrace();
          if (jdkBridgeChannel != null) {
            jdkBridgeChannel.close();
          }
        }
      }
    }
  }

  public synchronized boolean isRun() {
    return isRun;
  }

  public synchronized void setRun(boolean run) {
    isRun = run;
  }

  public final void bind(final SocketAddress localAddress, final int backlog) throws Exception {
    this.serverSocketChannel.bind(localAddress, backlog);
  }

  public void acceptEvent() {
    this.serverSocketChannel.acceptEvent();
  }
}
