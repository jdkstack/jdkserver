package org.jdkstack.jdkserver.tcp.core.core.client;

import java.util.UUID;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.codecs.NetworkMessage;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 12:16
 * @since 2021-03-03 12:16:00
 */
public class ClientChannelReadWriteHandler implements Handler<JdkClientSocketChannel> {

  @Override
  public void handle(JdkClientSocketChannel connection) {
    // 读取服务端发来的消息.
    connection.read(
        message -> {
          String body = message.getBody();
          System.out.println("客户接收到的数据:" + body);
          // 收到服务端消息后,再回复服务端一条消息.
          NetworkMessage msg = new NetworkMessage();
          msg.setPriority(0);
          msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
          msg.setType(121);
          msg.setBody(
              100000
                  + "我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻我是客户端哦嘻0");
          msg.setLength(19999);
          connection.write(msg);
        });
    // 主动向服务端发一条消息.
    NetworkMessage msg = new NetworkMessage();
    msg.setPriority(0);
    msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
    msg.setType(121);
    msg.setBody("服务端你好,我是客户端.");
    msg.setLength(19999);
    connection.write(msg);
  }
}
