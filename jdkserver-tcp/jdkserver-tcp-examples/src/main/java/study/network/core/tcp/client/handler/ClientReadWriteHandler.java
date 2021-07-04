package study.network.core.tcp.client.handler;

import java.util.UUID;
import study.core.future.Handler;
import study.network.codecs.NetworkMessage;
import study.network.core.socket.NetSocket;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 12:16
 * @since 2021-03-03 12:16:00
 */
public class ClientReadWriteHandler implements Handler<NetSocket> {
  private NetSocket connection;

  @Override
  public void handle(NetSocket connection) {
    this.connection = connection;
    // 读取数据.
    connection.handlerNetworkMessage(
        object -> {
          NetworkMessage msg1 = new NetworkMessage();
          msg1.setPriority(0);
          msg1.setSessionId(UUID.randomUUID().getMostSignificantBits());
          msg1.setType(121);
          // 写数据不能太大,不超过1024,否则解码报错.原因暂时不知道,但可以肯定是读取数据时,长度出现问题.
          // 通俗点说,是编码器包处理问题.
          msg1.setBody("十点四十");
          msg1.setLength(19999);
          connection.writeNetworkMessage(msg1);
          System.out.println("I received some bytes: " + object);
        });
    // 读写过程中异常处理.
    connection.exceptionHandler(e -> System.out.println(e.getMessage()));
  }

  public void write() {
    //
  }

  public void read() {
    //
  }

  public void close() {
    //
  }

  public void exception() {
    //
  }
}
