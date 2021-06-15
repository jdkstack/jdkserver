package org.study.network.core.socket;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 流输出
 *
 * @author admin
 */
public class StreamOutput extends OutputStream {

  private ByteBuf buffer;

  /** Create a new instance which use the given {@link ByteBuf} */
  public StreamOutput(ByteBuf buffer) {
    this.buffer = buffer;
  }

  @Override
  public void close() throws IOException {
    // Nothing to do
  }

  @Override
  public void flush() throws IOException {
    // nothing to do
  }

  @Override
  public void write(int b) throws IOException {
    buffer.writeByte(b);
  }

  @Override
  public void write(byte[] bytes) throws IOException {
    buffer.writeBytes(bytes);
  }

  @Override
  public void write(byte[] bytes, int srcIndex, int length) throws IOException {
    buffer.writeBytes(bytes, srcIndex, length);
  }

  ByteBuf getBuffer() {
    return buffer;
  }
}
