package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.ByteBuffer;
import org.study.network.codecs.Message;

public abstract class AbstractByteToMessageDecoderHandler<T> implements ChannelInboundHandler {

  public void read(ChannelHandlerContext ctx, ByteBuffer msg) throws Exception {
    // 解码.
    Message message = decode(ctx, msg);
    // 将包装好的对象传递下去.
    ctx.fireChannelRead(message);
  }

  protected abstract Message decode(ChannelHandlerContext ctx, ByteBuffer in) throws Exception;
}
