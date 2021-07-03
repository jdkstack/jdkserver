package org.jdkstack.jdkserver.tcp.core.channel;

import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeSocketChannel;
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
public class ServerChannelReadWriteHandler implements Handler<JdkBridgeSocketChannel> {

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    connection.read(
        message -> {
          String body = message.getBody();
          System.out.println("服务器接收到的数据:" + body);
        });
  }
}
