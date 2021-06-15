package org.jdkstack.jdkserver.tcp.core.channel.queue;

import java.nio.channels.SelectionKey;

public class SelectionKeyQueue extends AbstractQueue<SelectionKey> {

  @Override
  public String getTarget() {
    return null;
  }
}
