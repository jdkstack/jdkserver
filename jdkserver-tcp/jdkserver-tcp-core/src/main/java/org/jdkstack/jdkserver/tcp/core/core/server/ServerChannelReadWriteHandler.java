package org.jdkstack.jdkserver.tcp.core.core.server;

import java.util.UUID;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessage;
import org.jdkstack.jdkserver.tcp.core.future.Handler;
import org.jdkstack.jdkserver.tcp.core.core.bridge.JdkBridgeSocketChannel;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 12:16
 * @since 2021-03-03 12:16:00
 */
public class ServerChannelReadWriteHandler implements Handler<JdkBridgeSocketChannel> {

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    connection.read(
        message -> {
          String body = message.getBody();
          System.out.println("服务器接收到的数据:" + body);
          NetworkMessage msg = new NetworkMessage();
          msg.setPriority(0);
          msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
          msg.setType(121);
          // 写数据不能太大,不超过1024,否则解码报错.原因暂时不知道,但可以肯定是读取数据时,长度出现问题.
          // 通俗点说,是编码器包处理问题.目前采用应用数据包前四个字节保存应用数据包完整长度解决问题.最大数据包长度不能超过10KB,否则丢弃消息.
          msg.setBody(
              100000
                  + "我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻0");
          msg.setLength(19999);
          connection.write(msg);
        });
  }
}
