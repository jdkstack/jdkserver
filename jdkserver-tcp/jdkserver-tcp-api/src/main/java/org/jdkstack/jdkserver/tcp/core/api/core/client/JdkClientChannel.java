package org.jdkstack.jdkserver.tcp.core.api.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.channel.JdkChannel;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface JdkClientChannel extends JdkChannel {

  void readEvent();

  void readSsl() throws Exception;

  void readHandler() throws Exception;

  void read() throws Exception;

  void close();

  void write2(ByteBuffer msg);

  void write1(ByteBuffer msg);

  void write3(Message msg);

  void writeSsl(Message msg);

  void write(Message msg);

  void connectEvent();

  void finishConnect() throws IOException;

  void readEventUp();
}
