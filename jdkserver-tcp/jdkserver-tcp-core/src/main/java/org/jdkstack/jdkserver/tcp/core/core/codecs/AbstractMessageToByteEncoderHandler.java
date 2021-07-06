package org.jdkstack.jdkserver.tcp.core.core.codecs;

import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelOutboundHandler;

public abstract class AbstractMessageToByteEncoderHandler<T> implements ChannelOutboundHandler {

  public void write(ChannelHandlerContext ctx, Message msg) throws Exception {
    // 阻塞:直到缓冲区可写为止.
    while (true) {
      // 如果当前的缓冲区可写.
      if (ctx.isWritable()) {
        ByteBuffer out = encode(ctx, msg);
        // 将编码后的对象传递到Channel中,并调用Channel底层的write方法.
        ctx.handleWrite(out);
        break;
      }
    }
  }

  protected abstract ByteBuffer encode(ChannelHandlerContext ctx, Message msg) throws Exception;
}
