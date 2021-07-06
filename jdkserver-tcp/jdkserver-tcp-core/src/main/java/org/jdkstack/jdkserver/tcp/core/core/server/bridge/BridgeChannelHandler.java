package org.jdkstack.jdkserver.tcp.core.core.server.bridge;

import org.jdkstack.jdkserver.tcp.core.api.core.handler.Handler;
import org.jdkstack.jdkserver.tcp.core.core.handler.ChannelDuplexHandler;

public class BridgeChannelHandler extends ChannelDuplexHandler {
  private Handler<JdkBridgeSocketChannel> handler;

  private JdkBridgeSocketChannel JdkBridgeSocketChannel;

  public BridgeChannelHandler(Handler<JdkBridgeSocketChannel> handler) {
    this.handler = handler;
  }

  public void setChannel(JdkBridgeSocketChannel channel) {
    this.JdkBridgeSocketChannel = channel;
    handler.handle(channel);
  }
}
