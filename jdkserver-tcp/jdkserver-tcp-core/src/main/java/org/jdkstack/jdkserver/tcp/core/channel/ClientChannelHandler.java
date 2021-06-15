package org.jdkstack.jdkserver.tcp.core.channel;

import org.jdkstack.jdkserver.tcp.core.tcp.bridge.JdkBridgeSocketChannel;
import org.jdkstack.jdkserver.tcp.core.tcp.client.JdkClientSocketChannel;
import org.study.core.future.Handler;

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
