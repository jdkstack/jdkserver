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
public class BridgeChannelReadSslHandler implements Handler<JdkBridgeSocketChannel> {

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    //
    try {
      connection.readSsl();
    } catch (Exception e) {
      e.printStackTrace();
      try {
        connection.close();
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
