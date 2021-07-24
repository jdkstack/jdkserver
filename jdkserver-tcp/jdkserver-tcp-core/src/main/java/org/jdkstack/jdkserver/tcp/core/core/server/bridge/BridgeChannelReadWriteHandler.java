package org.jdkstack.jdkserver.tcp.core.core.server.bridge;

import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;

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
    connection.write2(connection::write2);
    connection.write(connection::write1);
    // 读取客户端发来的消息.
    connection.read(
        message -> {
          String body = message.getBody();
          //LOG.error("服务端收到的数据:{}", body);
          // 收到客户端消息后,再回复客户端一条消息.
        });
    // 主动向客户端发一条消息.
  }
}
