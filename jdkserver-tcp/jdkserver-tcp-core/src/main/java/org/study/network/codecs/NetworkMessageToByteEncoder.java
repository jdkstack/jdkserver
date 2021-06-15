package org.study.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * 网络消息编码器,用于自定义消息的生成.
 *
 * <p>将消息对象NetworkMessage转换成ByteBuf(Netty)对象.
 *
 * @author admin
 */
public class NetworkMessageToByteEncoder extends MessageToByteEncoder<Message> {

  /**
   * 网络消息编码器,用于自定义消息的生成.
   *
   * <p>将消息对象NetworkMessage转换成ByteBuf(Netty)对象.
   *
   * @param ctx netty的处理器上下文.
   * @param msg NetworkMessage消息对象.
   * @param buf Netty buf对象.
   * @exception Exception 抛出所有异常,由Netty框架自己捕获处理.
   * @author admin
   */
  @Override
  protected void encode(final ChannelHandlerContext ctx, final Message msg, final ByteBuf buf)
      throws Exception {
    int headerLength = 41;
    int bodyLength = 0;
    byte[] bodyBytes = null;
    final String body = msg.getBody();
    if (null != body) {
      bodyBytes = body.getBytes(StandardCharsets.UTF_8);
      bodyLength = bodyBytes.length;
    }
    final Map<String, Object> attachment = msg.getAttachments();
    int attachmentSize = attachment.size();
    int attachmentLength = 0;
    // 遍历附件消息.
    for (final Map.Entry<String, Object> entry : attachment.entrySet()) {
      final String key = entry.getKey();
      final String value = String.valueOf(entry.getValue());
      // 设置附件消息的key长度和内容.
      if (!Objects.isNull(key)) {
        // 使用UTF-8编码字符串.
        final byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        attachmentLength = attachmentLength + keyBytes.length;
      }
      // 设置附件消息的value长度和内容.
      if (!Objects.isNull(value)) {
        // 使用UTF-8编码字符串.
        final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        attachmentLength = attachmentLength + valueBytes.length;
      }
    }
    final String customMsg = msg.getCustomMsg();
    final byte[] bytes = customMsg.getBytes(StandardCharsets.UTF_8);
    int customMsgLength = bytes.length;
    int totalLength = headerLength + bodyLength + attachmentLength + customMsgLength + 1;
    buf.writeInt(totalLength);

    // 消息的第一个字节是大写的S,ASCII都是一个字节.
    buf.writeByte(83);
    // 消息的CRC校验.
    buf.writeInt(msg.getCrcCode());
    // 消息的长度.
    buf.writeInt(msg.getLength());
    // 消息的sessionId.
    buf.writeLong(msg.getSessionId());
    // 消息的类型.
    buf.writeInt(msg.getType());
    // 消息的优先级.
    buf.writeInt(msg.getPriority());
    // 消息创建时的时间戳.
    final long createTime = System.currentTimeMillis();
    buf.writeLong(createTime);
    // 消息最大保留时间10秒,否则消息无效.
    buf.writeInt(msg.getTimeout());
    // 写body长度和内容.
    if (null != body) {
      buf.writeInt(bodyLength);
      buf.writeBytes(bodyBytes);
    } else {
      buf.writeInt(0);
    }
    // 设置附件消息的长度.
    buf.writeInt(attachmentSize);
    // 遍历附件消息.
    for (final Map.Entry<String, Object> entry : attachment.entrySet()) {
      final String key = entry.getKey();
      final String value = String.valueOf(entry.getValue());
      // 设置附件消息的key长度和内容.
      if (!Objects.isNull(key)) {
        // 使用UTF-8编码字符串.
        final byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(keyBytes.length);
        buf.writeBytes(keyBytes);
      }
      // 设置附件消息的value长度和内容.
      if (!Objects.isNull(value)) {
        // 使用UTF-8编码字符串.
        final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(valueBytes.length);
        buf.writeBytes(valueBytes);
      }
    }
    // 写自定义消息长度和内容.
    buf.writeInt(customMsgLength);
    buf.writeBytes(bytes);
    // 消息的最后一个字节是大写的Y.
    buf.writeByte(89);
  }
}
