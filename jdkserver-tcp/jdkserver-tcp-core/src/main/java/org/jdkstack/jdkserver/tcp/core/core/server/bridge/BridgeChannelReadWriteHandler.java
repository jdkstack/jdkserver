package org.jdkstack.jdkserver.tcp.core.core.server.bridge;

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
public class BridgeChannelReadWriteHandler implements Handler<JdkBridgeSocketChannel> {

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    // 读取客户端发来的消息.
    connection.read(
        message -> {
          String body = message.getBody();
          System.out.println("服务器接收到的数据:" + body);
          // 收到客户端消息后,再回复客户端一条消息.
          NetworkMessage msg = new NetworkMessage();
          msg.setPriority(0);
          msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
          msg.setType(121);
          msg.setBody(
              100000
                  + "我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻我是服务器端哦嘻0");
          msg.setLength(19999);
          connection.write(msg);
        });
    // 主动向客户端发一条消息.
    NetworkMessage msg = new NetworkMessage();
    msg.setPriority(0);
    msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
    msg.setType(121);
    msg.setBody("客户端你好,我是服务端.");
    msg.setLength(19999);
    connection.write(msg);
  }
}
