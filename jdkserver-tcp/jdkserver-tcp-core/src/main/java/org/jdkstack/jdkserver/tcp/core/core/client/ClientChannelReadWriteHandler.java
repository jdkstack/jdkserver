package org.jdkstack.jdkserver.tcp.core.core.client;

import org.jdkstack.jdklog.logging.api.spi.Log;
import org.jdkstack.jdklog.logging.core.factory.LogFactory;
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
public class ClientChannelReadWriteHandler implements Handler<JdkClientSocketChannel> {

  /** . */
  private static final Log LOG = LogFactory.getLog(ClientChannelReadWriteHandler.class);

  @Override
  public void handle(JdkClientSocketChannel connection) {
    // 1.注册写处理器.
    connection.write2(connection::write2);
    // 1.注册写处理器.
    // 发送原始数据编码后的消息.
    connection.write(connection::write1);
    // 2.注册读处理器.
    connection.read(
        message -> {
          // 处理服务端返回的消息(解码后的消息).
          String body = message.getBody();
          LOG.error("客户接收到的数据:{}", body);
          // 3.收到服务端消息后,还可以向服务端发消息.
        });
    // 4.主动向服务端发消息.
  }
}
