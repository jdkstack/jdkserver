package org.jdkstack.jdkserver.tcp.core.api.core.handler;

import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface ChannelHandlerContext {

  void setReadHandler(Handler<Message> handler);

  void setWriteHandler(Handler<ByteBuffer> handler);

  void handleWrite(final ByteBuffer buffer) throws Exception;

  void handleWrite2(final ByteBuffer buffer) throws Exception;

  boolean isWritable();

  void handleRead(final Object msg) throws Exception;

  boolean isReadable();
}
