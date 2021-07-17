package org.jdkstack.jdkserver.tcp.core.api.ssl.socket;

import java.io.Closeable;
import java.io.IOException;

public interface SslSocketChannelRead extends Closeable {

  int read(byte[] buf, int off, int len) throws IOException;

  int available() throws IOException;

  boolean markSupported();

  void reset() throws IOException;

  long skip(long s) throws IOException;

  void close() throws IOException;

  int read(byte[] buf) throws IOException;

  int read() throws IOException;
}
