package org.jdkstack.jdkserver.tcp.core.api.core.client;

import java.io.IOException;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.JdkChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface JdkClientChannel extends JdkChannel {

  void readEvent();

  void read() throws Exception;

  void close();

  void write(Message msg);

  void connectEvent();

  void finishConnect() throws IOException;

  void readEventUp();
}
