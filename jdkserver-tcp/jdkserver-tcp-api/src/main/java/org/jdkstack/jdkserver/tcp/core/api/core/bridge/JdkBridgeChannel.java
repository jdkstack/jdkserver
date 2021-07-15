package org.jdkstack.jdkserver.tcp.core.api.core.bridge;


import java.io.IOException;
import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface JdkBridgeChannel {

  void readEventUp();

  void readEventDown();

  void readSsl() throws Exception;

  void readHandler() throws Exception;

  void read() throws Exception;

  void write3(Message msg);

  void write1(ByteBuffer msg);

  void writeSsl(Message msg);

  void write(Message msg);

  void close() throws Exception;

  void finishConnect() throws IOException;

  void shutdown();

  void configureBlocking(boolean b);
}
