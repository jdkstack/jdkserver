package org.jdkstack.jdkserver.tcp.core.core.server;

import org.jdkstack.jdkserver.tcp.core.core.handler.ChannelDuplexHandler;
import org.jdkstack.jdkserver.tcp.core.future.Handler;
import org.jdkstack.jdkserver.tcp.core.core.bridge.JdkBridgeSocketChannel;

public class ServerChannelHandler extends ChannelDuplexHandler {
  private Handler<JdkBridgeSocketChannel> handler;

  private JdkBridgeSocketChannel JdkBridgeSocketChannel;

  public ServerChannelHandler(Handler<JdkBridgeSocketChannel> handler) {
    this.handler = handler;
  }

  public void setChannel(JdkBridgeSocketChannel channel) {
    this.JdkBridgeSocketChannel = channel;
    handler.handle(channel);
  }
}
