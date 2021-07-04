package study.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网络消息解码器,用于自定义消息的生成.
 *
 * <p>将ByteBuf(Netty)对象转换成NetworkMessage消息对象.
 *
 * @author admin
 */
public class NetworkByteToMessageDecoder extends ByteToMessageDecoder {

  /**
   * 网络消息编码器,用于自定义消息的生成.
   *
   * <p>将消息对象NetworkMessage转换成ByteBuf(Netty)对象.
   *
   * @param ctx netty的处理器上下文.
   * @param in Netty buf对象.
   * @param out out 包装NetworkMessage消息对象.
   * @exception Exception 抛出所有异常,由Netty框架自己捕获处理.
   * @author admin
   */
  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
      throws Exception {
    int totalLength = in.readInt();
    // 获取消息的第一个字节大写的S.
    final byte start = in.readByte();
    // 如果第一个字节不是大写的S,ASCII码83,则消息是无效的.
    final int s = 83;
    if (s != start) {
      // 目前没有一个非常优雅的方法比较byte和char.
      ctx.fireChannelRead(in);
    }
    final Message msg = new NetworkMessage();
    // 设置CRC校验.
    msg.setCrcCode(in.readInt());
    // 设置消息长度.
    msg.setLength(in.readInt());
    // 设置消息sessionId.
    msg.setSessionId(in.readLong());
    // 设置消息类型.
    msg.setType(in.readInt());
    // 设置消息优先级.
    msg.setPriority(in.readInt());
    // 消息创建的时间.
    msg.setCreateTime(in.readLong());
    // 消息的超时时间.
    msg.setTimeout(in.readInt());
    // 读取消息的body.
    final int bodyLen = in.readInt();
    int i1 = in.readerIndex();
    int i2 = in.readableBytes();
    if (0 < bodyLen) {
      final byte[] bodyBytes = new byte[bodyLen];
      in.readBytes(bodyBytes);
      final String body = new String(bodyBytes, StandardCharsets.UTF_8);
      msg.setBody(body);
    }
    // 可选附件消息字段的长度
    final int size = in.readInt();
    if (0 < size) {
      // 循环附件消息的个数.
      for (int i = 0; i < size; i++) {
        // 循环中new对象的提示,暂时无法解决Array allocation in loop .
        final byte[] keyBytes = new byte[in.readInt()];
        in.readBytes(keyBytes);
        final String key = new String(keyBytes, StandardCharsets.UTF_8);
        final byte[] valueBytes = new byte[in.readInt()];
        in.readBytes(valueBytes);
        final String value = new String(valueBytes, StandardCharsets.UTF_8);
        msg.attachment(key, value);
      }
    }
    // 读取自定义消息.
    final int customMsgLen = in.readInt();
    if (0 < customMsgLen) {
      final byte[] customMsgBytes = new byte[customMsgLen];
      in.readBytes(customMsgBytes);
      final String customMsg = new String(customMsgBytes, StandardCharsets.UTF_8);
      msg.setCustomMsg(customMsg);
    }
    // 获取消息的最后一个大写的字节Y.
    final byte end = in.readByte();
    // 如果最后一个字节不是大写的Y,ASCII码89,则消息是无效的.
    final int e = 89;
    if (e != end) {
      // 目前没有一个非常优雅的方法比较byte和char.
      ctx.fireChannelRead(in);
    }
    out.add(msg);
  }
}
