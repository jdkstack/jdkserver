package org.jdkstack.jdkserver.tcp.core.api.ssl.socket;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface SslSocketChannelWrite extends Closeable, Flushable {

  void write(int b) throws IOException;

  void write(byte b[]) throws IOException;

  void write(byte b[], int off, int len) throws IOException;

  void flush() throws IOException;

  void close() throws IOException;
}
