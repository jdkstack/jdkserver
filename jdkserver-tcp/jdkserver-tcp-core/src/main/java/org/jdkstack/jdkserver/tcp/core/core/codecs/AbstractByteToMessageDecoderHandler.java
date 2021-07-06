package org.jdkstack.jdkserver.tcp.core.core.codecs;

import java.nio.ByteBuffer;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelInboundHandler;

public abstract class AbstractByteToMessageDecoderHandler<T> implements ChannelInboundHandler {

  public void read(ChannelHandlerContext ctx, ByteBuffer in) throws Exception {
    // 阻塞:直到缓冲区可读为止.
    while (true) {
      // 如果当前的缓冲区可读.
      if (ctx.isReadable()) {
        // 将ByteBuffer解码成Message对象.
        Message msg = decode(ctx, in);
        // 将解码后的对象传递到业务处理器中.
        ctx.handleRead(msg);
        break;
      }
    }
  }

  protected abstract Message decode(ChannelHandlerContext ctx, ByteBuffer in) throws Exception;
}
