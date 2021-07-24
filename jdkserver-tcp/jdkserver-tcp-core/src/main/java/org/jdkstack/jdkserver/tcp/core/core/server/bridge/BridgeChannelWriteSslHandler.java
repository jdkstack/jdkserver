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
public class BridgeChannelWriteSslHandler implements Handler<JdkBridgeSocketChannel> {

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    for (int i = 0; i < 10; i++) {
      // 主动向服务端发一条消息.
      NetworkMessage msg = new NetworkMessage();
      msg.setPriority(0);
      msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
      msg.setType(121);
      msg.setBody("客户端你好,我是服务端." + i);
      msg.setLength(19999);
      connection.writeSsl(msg);
    }
  }
}
