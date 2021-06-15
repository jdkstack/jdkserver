package org.study.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.nio.charset.StandardCharsets;

/**
 * ByteToMessage解码器.
 *
 * @author admin
 */
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param maxFrameLength .
   * @param lengthFieldOffset .
   * @param lengthFieldLength .
   * @author admin
   */
  public NettyMessageDecoder(
      final int maxFrameLength, final int lengthFieldOffset, final int lengthFieldLength) {
    super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param ctx .
   * @param in .
   * @author admin
   */
  @Override
  protected Object decode(final ChannelHandlerContext ctx, final ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf) super.decode(ctx, in);
    NetworkMessage msg = new NetworkMessage();
    msg.setCrcCode(frame.readInt());
    msg.setLength(frame.readInt());
    msg.setSessionId(frame.readLong());
    msg.setType(frame.readByte());
    msg.setPriority(frame.readByte());
    int size = 4;
    if (frame.readableBytes() > size) {
      int i = frame.readInt();
      byte[] bytes = new byte[i];
      frame.readBytes(bytes);
      msg.setBody(new String(bytes, StandardCharsets.UTF_8));
    }
    return msg;
  }
}
