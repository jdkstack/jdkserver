package org.study.network.core.tcp.server.handler;

import java.util.UUID;
import org.study.core.future.Handler;
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
public class ServerReadWriteHandler implements Handler<NetSocket> {

  @Override
  public void handle(NetSocket connection) {
    NetworkMessage msg = new NetworkMessage();
    msg.setPriority((byte) 0);
    msg.setSessionId(UUID.randomUUID().getMostSignificantBits());
    msg.setType((byte) 121);
    msg.setBody("我是服务器端哦嘻");
    msg.setLength(19999);
    connection.writeNetworkMessage(msg);
    connection.handlerNetworkMessage(
        object -> {
          NetworkMessage msg1 = new NetworkMessage();
          msg1.setPriority((byte) 0);
          msg1.setSessionId(UUID.randomUUID().getMostSignificantBits());
          msg1.setType((byte) 121);
          msg1.setBody("我是服务器端哦嘻");
          msg1.setLength(19999);
          connection.writeNetworkMessage(msg1);
          System.out.println("I received some bytes: "+ object);
        });
    connection.exceptionHandler(e -> System.out.println(e.getMessage()));
  }
}
