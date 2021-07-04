package org.jdkstack.jdkserver.tcp.core.core.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessage;
import org.jdkstack.jdkserver.tcp.core.core.bridge.JdkBridgeChannel;
import org.jdkstack.jdkserver.tcp.core.core.bridge.JdkBridgeSocketChannel;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class JdkServerSocketChannelEventRunnable implements Runnable {
  private JdkServerSocketChannel serverSocketChannel;
  // private JdkServerSocketChannelMessageHandler messageHandler;
  private volatile boolean isRun;

  public JdkServerSocketChannelEventRunnable(JdkServerSocketChannel serverSocketChannel) {
    this.serverSocketChannel = serverSocketChannel;
    //  this.messageHandler = messageHandler;
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
    while (!isRun()) {
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
      // keys.clear();
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
        NetworkMessage msg = new NetworkMessage();
        msg.setPriority(0);
        msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
        msg.setType(121);
        // 写数据不能太大,不超过1024,否则解码报错.原因暂时不知道,但可以肯定是读取数据时,长度出现问题.
        // 通俗点说,是编码器包处理问题.目前采用应用数据包前四个字节保存应用数据包完整长度解决问题.最大数据包长度不能超过10KB,否则丢弃消息.
        msg.setBody(
            100000
                + "我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻");
        msg.setLength(19999);
        jdkBridgeChannel.write(msg);
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
