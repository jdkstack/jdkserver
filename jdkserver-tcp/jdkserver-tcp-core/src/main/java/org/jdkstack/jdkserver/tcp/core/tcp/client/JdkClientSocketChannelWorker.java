package org.jdkstack.jdkserver.tcp.core.tcp.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import org.jdkstack.jdkserver.tcp.core.channel.JdkClientChannel;

public class JdkClientSocketChannelWorker implements Runnable {
  private final JdkClientSocketChannel clientSocketChannel;

  public JdkClientSocketChannelWorker(JdkClientSocketChannel clientSocketChannel) {
    this.clientSocketChannel = clientSocketChannel;
  }

  public static void main(String[] args) throws Exception {
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 20000);
    SocketAddress localAddress = new InetSocketAddress("127.0.0.1", 18000);
    JdkClientSocketChannel jdkClientSocketChannel = new JdkClientSocketChannel();
    JdkClientSocketChannelWorker jdkClientSocketChannelWorker =
        new JdkClientSocketChannelWorker(jdkClientSocketChannel);
    jdkClientSocketChannelWorker.connectEvent();
    jdkClientSocketChannelWorker.connect(remoteAddress, localAddress);
    jdkClientSocketChannelWorker.run();
  }

  @Override
  public void run() {
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

  public void connect(final SocketAddress remoteAddress) throws Exception {
    this.clientSocketChannel.connect(remoteAddress);
  }

  public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress)
      throws Exception {
    this.clientSocketChannel.connect(remoteAddress, localAddress);
  }

  private void connectEvent() {
    this.clientSocketChannel.connectEvent();
  }

  private void readEvent() {
    this.clientSocketChannel.readEventUp();
  }
}
