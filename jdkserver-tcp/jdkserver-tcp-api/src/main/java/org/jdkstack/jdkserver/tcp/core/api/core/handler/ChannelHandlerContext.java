package org.jdkstack.jdkserver.tcp.core.api.core.handler;

import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;

public interface ChannelHandlerContext {

  void setReadHandler(Handler<Message> handler);

  void setWriteHandler(Handler<Message> handler);

  void handleWrite(final ByteBuffer buffer) throws Exception;

  boolean isWritable();

  void handleRead(final Object msg) throws Exception;

  boolean isReadable();
}
