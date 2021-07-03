package org.jdkstack.jdkserver.tcp.core.channel;

import org.jdkstack.jdkserver.tcp.core.tcp.client.JdkClientSocketChannel;
import org.study.core.future.Handler;

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
    connection.read(
        message -> {
          String body = message.getBody();
          System.out.println("客户接收到的数据:" + body);
        });
  }
}
