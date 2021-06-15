package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.ByteBuffer;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;

public abstract class AbstractByteToMessageDecoderHandler<T> implements ChannelInboundHandler {

  public void read(ChannelHandlerContext ctx, ByteBuffer msg) throws Exception {
    // 只处理单条消息.
    Message message = new NetworkMessage();
    // 解码.
    decode(ctx, msg, message);
    // 将包装好的对象传递下去.
    ctx.fireChannelRead(message);
  }

  protected abstract void decode(ChannelHandlerContext ctx, ByteBuffer in, Message out)
      throws Exception;
}
