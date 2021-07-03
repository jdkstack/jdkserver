package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.ByteBuffer;
import org.study.network.codecs.Message;

public abstract class AbstractMessageToByteEncoderHandler<T> implements ChannelOutboundHandler {

  public void write(ChannelHandlerContext ctx, Message msg) throws Exception {
    ByteBuffer buf = null;
    try {
      buf = encode(ctx, msg);
      // 写出去.
      ctx.write(buf);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (buf != null) {
        //
      }
    }
  }

  protected abstract ByteBuffer encode(ChannelHandlerContext ctx, Message msg) throws Exception;
}
