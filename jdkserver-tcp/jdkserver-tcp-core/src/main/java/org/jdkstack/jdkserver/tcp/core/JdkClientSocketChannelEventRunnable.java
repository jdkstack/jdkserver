package org.jdkstack.jdkserver.tcp.core;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.jdkstack.jdkserver.tcp.core.channel.JdkClientChannel;
import org.jdkstack.jdkserver.tcp.core.tcp.client.JdkClientSocketChannel;
import org.study.network.codecs.NetworkMessage;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class JdkClientSocketChannelEventRunnable implements Runnable {
  public JdkClientSocketChannel clientSocketChannel;
  public JdkServerSocketChannelMessageHandler messageHandler;
  public volatile boolean isRun;

  public JdkClientSocketChannelEventRunnable(
      JdkClientSocketChannel clientSocketChannel,
      JdkServerSocketChannelMessageHandler messageHandler) {
    this.clientSocketChannel = clientSocketChannel;
    this.messageHandler = messageHandler;
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
        events = this.clientSocketChannel.select(500);
      } catch (final IOException e) {
        e.printStackTrace();
      }
      // 事件大于0.
      if (0 >= events) {
        continue;
      }
      // 获取服务器端接收到的事件key的集合.
      final Set<SelectionKey> keys = this.clientSocketChannel.selectedKeys();
      // 获取集合的迭代对象.
      final Iterator<SelectionKey> it = keys.iterator();
      // 是否存在一个事件.
      while (it.hasNext()) {
        // 获取一个事件.
        final SelectionKey key = it.next();
        // 事件绑定的channel对象(事件绑定三种channel:JdkServerChannel->JdkBridgeChannel<-JdkClientChannel)
        final Object channel = key.attachment();
        // 获取事件的常量,1表示OP_READ事件,8表示OP_CONNECT事件.
        final int readyOps = key.readyOps();
        try {
          if (channel instanceof JdkClientChannel) {
            JdkClientChannel jdkClientChannel = (JdkClientChannel) channel;
            // 处理客户端channel事件,主要是OP_CONNECT事件.
            handlerJdkClientChannel(key, readyOps, jdkClientChannel);
          }
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

  private void handlerJdkClientChannel(
      SelectionKey key, int readyOps, JdkClientChannel jdkClientChannel) {
    // 如果key是有效的.
    if (key.isValid()) {
      if ((readyOps & SelectionKey.OP_CONNECT) != 0 || readyOps == 0) {
        try {
          int ops = key.interestOps();
          ops &= ~SelectionKey.OP_CONNECT;
          key.interestOps(ops);
          jdkClientChannel.finishConnect();
        } catch (final Exception e) {
          e.printStackTrace();
          jdkClientChannel.close();
        }
        try {
          jdkClientChannel.readEventUp();
        } catch (final Exception e) {
          e.printStackTrace();
          jdkClientChannel.close();
        }
        NetworkMessage msg = new NetworkMessage();
        msg.setPriority(0);
        msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
        msg.setType(121);
        // 写数据不能太大,不超过1024,否则解码报错.原因暂时不知道,但可以肯定是读取数据时,长度出现问题.
        // 通俗点说,是编码器包处理问题.
        msg.setBody(
            100000
                + "我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻");
        msg.setLength(19999);
        jdkClientChannel.write(msg);
      }

      if ((readyOps & SelectionKey.OP_WRITE) != 0 || readyOps == 0) {
        try {
          jdkClientChannel.write(null);
        } catch (final Exception e) {
          e.printStackTrace();
          jdkClientChannel.close();
        }
      }

      if ((readyOps & (SelectionKey.OP_READ)) != 0 || readyOps == 0) {
        try {
          jdkClientChannel.read();
        } catch (final Exception e) {
          e.printStackTrace();
          jdkClientChannel.close();
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

  public void connect(final SocketAddress remoteAddress) throws Exception {
    this.clientSocketChannel.connect(remoteAddress);
  }

  public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress)
      throws Exception {
    this.clientSocketChannel.connect(remoteAddress, localAddress);
  }

  public void connectEvent() {
    this.clientSocketChannel.connectEvent();
  }
}
