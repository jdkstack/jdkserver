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
public class ClientChannelReadWriteSslHandler implements Handler<JdkClientSocketChannel> {

  /** . */
  private static final Log LOG = LogFactory.getLog(ClientChannelReadWriteSslHandler.class);

  @Override
  public void handle(JdkClientSocketChannel connection) {
    //
  }
}
