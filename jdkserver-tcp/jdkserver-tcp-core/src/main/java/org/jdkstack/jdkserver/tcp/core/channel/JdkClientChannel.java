package org.jdkstack.jdkserver.tcp.core.channel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;

public interface JdkClientChannel extends JdkChannel {

  void readEvent();

  void read() throws Exception;

  void close();

  void write(Message msg);

  void connectEvent();

  void finishConnect() throws IOException;

  void readEventUp();
}
