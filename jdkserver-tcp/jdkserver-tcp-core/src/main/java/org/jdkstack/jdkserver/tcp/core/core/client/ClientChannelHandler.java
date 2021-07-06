package org.jdkstack.jdkserver.tcp.core.core.client;

import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.handler.ChannelDuplexHandler;

public class ClientChannelHandler extends ChannelDuplexHandler {
  private Handler<JdkClientSocketChannel> handler;

  private JdkClientSocketChannel JdkBridgeSocketChannel;

  public ClientChannelHandler(Handler<JdkClientSocketChannel> handler) {
    this.handler = handler;
  }

  public void setChannel(JdkClientSocketChannel channel) {
    this.JdkBridgeSocketChannel = channel;
    handler.handle(channel);
  }
}
