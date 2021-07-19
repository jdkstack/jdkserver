package org.jdkstack.jdkserver.tcp.core.core.server.bridge;

import org.jdkstack.jdklog.logging.api.spi.Log;
import org.jdkstack.jdklog.logging.core.factory.LogFactory;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.client.JdkClientSocketChannel;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 12:16
 * @since 2021-03-03 12:16:00
 */
public class BridgeChannelReadHandler implements Handler<JdkBridgeSocketChannel> {

  /** . */
  private static final Log LOG = LogFactory.getLog(BridgeChannelReadHandler.class);

  @Override
  public void handle(JdkBridgeSocketChannel connection) {
    try {
      connection.read();
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
