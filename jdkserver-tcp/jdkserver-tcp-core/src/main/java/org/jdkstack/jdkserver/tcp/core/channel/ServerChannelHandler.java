package org.jdkstack.jdkserver.tcp.core.channel;

import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeSocketChannel;
import org.study.core.future.Handler;

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
