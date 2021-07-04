package org.jdkstack.jdkserver.tcp.core.core.handler;

import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.future.Handler;

public interface ChannelHandlerContext {

  void setHandler(Handler<Message> handler);

  void setWriteHandler(Handler<Message> handler);

  void handleWrite(final ByteBuffer buffer) throws Exception;

  boolean isWritable();

  void handleRead(final Object msg) throws Exception;

  boolean isReadable();
}
