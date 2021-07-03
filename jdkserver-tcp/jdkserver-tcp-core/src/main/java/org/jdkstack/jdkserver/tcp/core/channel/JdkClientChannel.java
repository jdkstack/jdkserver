package org.jdkstack.jdkserver.tcp.core.channel;

import java.io.IOException;
import org.study.network.codecs.Message;

public interface JdkClientChannel extends JdkChannel {

  void readEvent();

  void read() throws Exception;

  void close();

  void write(Message msg);

  void connectEvent();

  void finishConnect() throws IOException;

  void readEventUp();
}
