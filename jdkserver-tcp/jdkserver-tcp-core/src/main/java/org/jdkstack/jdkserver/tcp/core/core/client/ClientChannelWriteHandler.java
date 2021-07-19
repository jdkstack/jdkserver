package org.jdkstack.jdkserver.tcp.core.core.client;

import java.util.UUID;
import org.jdkstack.jdklog.logging.api.spi.Log;
import org.jdkstack.jdklog.logging.core.factory.LogFactory;
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
public class ClientChannelWriteHandler implements Handler<JdkClientSocketChannel> {

  /** . */
  private static final Log LOG = LogFactory.getLog(ClientChannelWriteHandler.class);

  @Override
  public void handle(JdkClientSocketChannel connection) {
    for (int i = 0; i <= 159900; i++) {
      // 主动向服务端发一条消息.
      NetworkMessage msg = new NetworkMessage();
      msg.setPriority(0);
      msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
      msg.setType(121);
      msg.setBody("服务端你好,我是客户端." + i);
      msg.setLength(19999);
      connection.write(msg);
    }
  }
}