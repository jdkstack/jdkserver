package org.jdkstack.jdkserver.tcp.core.udp.client;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import org.jdkstack.jdkserver.tcp.core.channel.AbstractJdkChannel;
import org.jdkstack.jdkserver.tcp.core.channel.ChannelException;

public class JdkClientDatagramChannel extends AbstractJdkChannel {

  private DatagramChannel datagramChannel() {
    try {
      return DEFAULT_SELECTOR_PROVIDER.openDatagramChannel();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a socket.", e);
    }
  }
}
