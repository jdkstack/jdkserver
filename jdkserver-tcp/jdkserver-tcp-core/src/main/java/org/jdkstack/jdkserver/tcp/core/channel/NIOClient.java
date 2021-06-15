package org.jdkstack.jdkserver.tcp.core.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.study.network.codecs.NetworkMessage;

public class NIOClient {
  public static void main(String[] args) throws Exception {
    // 打开选择器
    Selector selector = Selector.open();
    // 打开通道
    SocketChannel socketChannel = SocketChannel.open();
    // 配置非阻塞模型
    socketChannel.configureBlocking(false);
    // 连接远程主机
    socketChannel.connect(new InetSocketAddress("127.0.0.1", 20000));
    // 注册事件
    socketChannel.register(selector, SelectionKey.OP_CONNECT);
    // 循环处理
    for (; ; ) {
      // 获取当前服务器端channel接收到的的事件.
      int events = 0;
      try {
        events = selector.select(500);
      } catch (final IOException e) {
        e.printStackTrace();
      }
      if (0 >= events) {
        continue;
      }
      Set<SelectionKey> keys = selector.selectedKeys();
      Iterator<SelectionKey> iter = keys.iterator();
      while (iter.hasNext()) {
        SelectionKey key = iter.next();
        final int readyOps = key.readyOps();
        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
          int ops = key.interestOps();
          ops &= ~SelectionKey.OP_CONNECT;
          key.interestOps(ops);
        }
        /*        final int readyOps = key.readyOps();
        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
          System.out.println("OP_CONNECT");
        }
        if ((readyOps & SelectionKey.OP_WRITE) != 0) {
          System.out.println("OP_WRITE");
        }
        if ((readyOps & SelectionKey.OP_READ) != 0) {
          System.out.println("OP_READ");
        }
        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
          System.out.println("OP_ACCEPT");
        }*/
        if (key.isConnectable()) {
          // 连接建立或者连接建立不成功
          SocketChannel channel = (SocketChannel) key.channel();
          // 完成连接的建立
          if (channel.finishConnect()) {
            channel.register(selector, SelectionKey.OP_READ);
            for (int i = 0; i < 20; i++) {
              NetworkMessage msg = new NetworkMessage();
              msg.setPriority(0);
              msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
              msg.setType(121);
              // 写数据不能太大,不超过1024,否则解码报错.原因暂时不知道,但可以肯定是读取数据时,长度出现问题.
              // 通俗点说,是编码器包处理问题.
              msg.setBody(
                  i * 100000
                      + "我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻");
              msg.setLength(19999);

              int headerLength = 41;
              int bodyLength = 0;
              byte[] bodyBytes = null;
              final String body = msg.getBody();
              if (null != body) {
                bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                bodyLength = bodyBytes.length;
              }
              final Map<String, Object> attachment = msg.getAttachments();
              int attachmentSize = attachment.size();
              int attachmentLength = 0;
              // 遍历附件消息.
              for (final Map.Entry<String, Object> entry : attachment.entrySet()) {
                final String key1 = entry.getKey();
                final String value = String.valueOf(entry.getValue());
                // 设置附件消息的key长度和内容.
                if (!Objects.isNull(key1)) {
                  // 使用UTF-8编码字符串.
                  final byte[] keyBytes = key1.getBytes(StandardCharsets.UTF_8);
                  attachmentLength = attachmentLength + keyBytes.length;
                }
                // 设置附件消息的value长度和内容.
                if (!Objects.isNull(value)) {
                  // 使用UTF-8编码字符串.
                  final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
                  attachmentLength = attachmentLength + valueBytes.length;
                }
              }
              final String customMsg = msg.getCustomMsg();
              final byte[] bytes = customMsg.getBytes(StandardCharsets.UTF_8);
              int customMsgLength = bytes.length;
              int totalLength =
                  headerLength
                      + bodyLength
                      + attachmentLength
                      + customMsgLength
                      + 1
                      + 12
                      + attachmentSize * 4 * 2;
              ByteBuffer buf = ByteBuffer.allocate(totalLength);
              buf.putInt(totalLength);
              // 消息的第一个字节是大写的S,ASCII都是一个字节.
              buf.put((byte) 83);
              // 消息的CRC校验.
              buf.putInt(msg.getCrcCode());
              // 消息的长度.
              buf.putInt(msg.getLength());
              // 消息的sessionId.
              buf.putLong(msg.getSessionId());
              // 消息的类型.
              buf.putInt(msg.getType());
              // 消息的优先级.
              buf.putInt(msg.getPriority());
              // 消息创建时的时间戳.
              final long createTime = System.currentTimeMillis();
              buf.putLong(createTime);
              // 消息最大保留时间10秒,否则消息无效.
              buf.putInt(msg.getTimeout());
              // 写body长度和内容.
              if (null != body) {
                buf.putInt(bodyLength);
                buf.put(bodyBytes);
              } else {
                buf.putInt(0);
              }
              // 设置附件消息的长度.
              buf.putInt(attachmentSize);
              // 遍历附件消息.
              for (final Map.Entry<String, Object> entry : attachment.entrySet()) {
                final String key1 = entry.getKey();
                final String value = String.valueOf(entry.getValue());
                // 设置附件消息的key长度和内容.
                if (!Objects.isNull(key1)) {
                  // 使用UTF-8编码字符串.
                  final byte[] keyBytes = key1.getBytes(StandardCharsets.UTF_8);
                  buf.putInt(keyBytes.length);
                  buf.put(keyBytes);
                }
                // 设置附件消息的value长度和内容.
                if (!Objects.isNull(value)) {
                  // 使用UTF-8编码字符串.
                  final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
                  buf.putInt(valueBytes.length);
                  buf.put(valueBytes);
                }
              }
              // 写自定义消息长度和内容.
              buf.putInt(customMsgLength);
              buf.put(bytes);
              // 消息的最后一个字节是大写的Y.
              buf.put((byte) 89);
              try {
                ByteBuffer buffer = ByteBuffer.wrap(buf.array());
                channel.write(buffer);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
        if (key.isReadable()) {
          SocketChannel channel = (SocketChannel) key.channel();
          /*创建ByteBuffer，并开辟一个1k的缓冲区*/
          ByteBuffer buffer = ByteBuffer.allocate(128);
          /*将通道的数据读取到缓冲区，read方法返回读取到的字节数*/
          int readBytes = channel.read(buffer);
          if (readBytes > 0) {
            buffer.flip();
            byte[] bytes1 = new byte[buffer.remaining()];
            buffer.get(bytes1);
            String result = new String(bytes1, "UTF-8");
            System.out.println("客户端收到消息：" + result);
            // 这里注册写事件，因为写事件基本都处于就绪状态；
            // 从处理逻辑来看，一般接收到客户端读事件时也会伴随着写，类似HttpServletRequest和HttpServletResponse
            // key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
          }
        }
        /*  if (key.isWritable()) {
          //
          System.out.println("client isWritable");
        }*/
        iter.remove();
      }
    }
  }
}
