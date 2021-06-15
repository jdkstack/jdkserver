package org.jdkstack.jdkserver.tcp.core.channel;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface JdkServerChannel extends JdkChannel {

  void bind(final SocketAddress localAddress, final int backlog) throws Exception;

  SelectionKey register(final Selector selector, final int ops);

  void init();

  void acceptEvent();

  void accept();

  void acceptEventUp();

  void acceptEventDown();

  void readEventUp();

  void readEventDown();
}
