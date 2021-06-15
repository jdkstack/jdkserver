package org.jdkstack.jdkserver.tcp.core.tcp.bridge;

import java.io.IOException;
import org.study.network.codecs.Message;

public interface JdkBridgeChannel {
  void readEventUp();

  void readEventDown();

  void read() throws Exception;

  void write(Message msg);

  void close();
}
