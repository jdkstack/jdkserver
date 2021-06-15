package org.jdkstack.jdkserver.tcp.core.channel.codecs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jdkstack.jdkserver.tcp.core.channel.AbstractByteToMessageDecoderHandler;
import org.jdkstack.jdkserver.tcp.core.channel.ChannelHandlerContext;

import org.study.network.codecs.Message;
/**
 * 网络消息解码器,用于自定义消息的生成.
 *
 * <p>将ByteBuf(Netty)对象转换成NetworkMessage消息对象.
 *
 * @author admin
 */
public class NetworkByteToMessageDecoderHandler
    extends AbstractByteToMessageDecoderHandler<Message> {

  /**
   * 网络消息编码器,用于自定义消息的生成.
   *
   * <p>将消息对象NetworkMessage转换成ByteBuf(Netty)对象.
   *
   * @param ctx netty的处理器上下文.
   * @param frame Netty buf对象.
   * @param out out 包装NetworkMessage消息对象.
   * @exception Exception 抛出所有异常,由Netty框架自己捕获处理.
   * @author admin
   */
  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuffer body, final Message msg)
      throws Exception {
    byte b = body.get();
    int anInt1 = body.getInt();
    int anInt2 = body.getInt();
    long aLong = body.getLong();
    int anInt11 = body.getInt();
    int anInt21 = body.getInt();
    long aLong1 = body.getLong();
    int anInt211 = body.getInt();
    int anInt2111 = body.getInt();
    if (anInt2111 > 0) {
      byte[] bytes111 = new byte[anInt2111];
      body.get(bytes111);
      msg.setBody(new String(bytes111, StandardCharsets.UTF_8));
    }
    int anInt21111 = body.getInt();

    int anInt21111111 = body.getInt();
    byte[] bytes1111111 = new byte[anInt21111111];
    body.get(bytes1111111);
    byte b1 = body.get();

  }
}
