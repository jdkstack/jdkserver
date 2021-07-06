package org.jdkstack.jdkserver.tcp.core.api.core.bridge;


import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface JdkBridgeChannel {

  void readEventUp();

  void readEventDown();

  void read() throws Exception;

  void write(Message msg);

  void close();
}
