package org.jdkstack.jdkserver.tcp.core.core.client;

import org.jdkstack.jdkserver.tcp.core.core.handler.ChannelDuplexHandler;
import org.jdkstack.jdkserver.tcp.core.future.Handler;

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
