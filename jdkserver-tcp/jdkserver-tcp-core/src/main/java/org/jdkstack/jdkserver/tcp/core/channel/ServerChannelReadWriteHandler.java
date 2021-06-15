package org.jdkstack.jdkserver.tcp.core.channel;

import java.util.UUID;
import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeSocketChannel;
import org.study.core.future.Handler;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;
import org.study.network.core.socket.NetSocket;

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
