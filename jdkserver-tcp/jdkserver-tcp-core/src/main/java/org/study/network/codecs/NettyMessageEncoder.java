package org.study.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MessageToByte编码器.
 *
 * @author admin
 */
public class NettyMessageEncoder extends MessageToByteEncoder<NetworkMessage> {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  protected void encode(
      final ChannelHandlerContext ctx, final NetworkMessage msg, final ByteBuf sendBuf)
      throws Exception {
    final int crcCode = msg.getCrcCode();
    sendBuf.writeInt(crcCode);
    final int length = msg.getLength();
    sendBuf.writeInt(length);
    final long sessionId = msg.getSessionId();
    sendBuf.writeLong(sessionId);
    final int type = msg.getType();
    sendBuf.writeByte(type);
    final int priority = msg.getPriority();
    sendBuf.writeByte(priority);
    final String body = msg.getBody();
    if (body != null) {
      final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      sendBuf.writeInt(bytes.length);
      sendBuf.writeBytes(bytes);
    } else {
      sendBuf.writeInt(0);
    }
    final int i = sendBuf.readableBytes();
    sendBuf.setInt(4, i - 8);
  }
}
