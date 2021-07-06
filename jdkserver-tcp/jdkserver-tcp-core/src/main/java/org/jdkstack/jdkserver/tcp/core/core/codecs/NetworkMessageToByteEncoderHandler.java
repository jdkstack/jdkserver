package org.jdkstack.jdkserver.tcp.core.core.codecs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;

/**
 * 编码器.
 *
 * <p>将消息对象Message转换成ByteBuffer对象.
 *
 * @author admin
 */
public class NetworkMessageToByteEncoderHandler
    extends AbstractMessageToByteEncoderHandler<Message> {

  /**
   * 将消息对象Message转换成ByteBuffer对象.
   *
   * <p>编码规则
   *
   * @param ctx 上下文.
   * @param msg Message消息对象.
   * @exception Exception 抛出所有异常.
   * @author admin
   */
  @Override
  protected ByteBuffer encode(final ChannelHandlerContext ctx, final Message msg) throws Exception {
    // 计算整个消息报文长度.
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
      final String key1 = entry.getKey();
      final String value = String.valueOf(entry.getValue());
      // 设置附件消息的key长度和内容.
      if (!Objects.isNull(key1)) {
        // 使用UTF-8编码字符串.
        final byte[] keyBytes = key1.getBytes(StandardCharsets.UTF_8);
        attachmentLength += keyBytes.length;
      }
      // 设置附件消息的value长度和内容.
      if (!Objects.isNull(value)) {
        // 使用UTF-8编码字符串.
        final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        attachmentLength += valueBytes.length;
      }
    }
    final String customMsg = msg.getCustomMsg();
    final byte[] bytes = customMsg.getBytes(StandardCharsets.UTF_8);
    // 自定义消息长度.
    int customMsgLength = bytes.length;
    // 固定头部长度.
    int headerLength = 41;
    // 整个单行报文长度.
    int totalLength =
        headerLength
            + bodyLength
            + attachmentLength
            + customMsgLength
            + 1
            + 12
            + attachmentSize * 4 * 2;
    // 用整个单行报文长度创建ByteBuffer() 报文总大小不能太大,目前没有限制,但是最好10KB以内.
    ByteBuffer buf = ByteBuffer.allocate(totalLength);
    // 单行报文的总长度.
    buf.putInt(totalLength);
    // 消息的第一个字节是大写的S,ASCII都是一个字节.
    buf.put(Constants.START);
    // 消息的CRC校验.
    buf.putInt(msg.getCrcCode());
    // 消息的长度.
    buf.putInt(msg.getLength());
    // 消息的sessionId.
    buf.putLong(msg.getSessionId());
    // 消息的类型.
    buf.putInt(msg.getType());
    // 消息的优先级.
    buf.putInt(msg.getPriority());
    // 消息创建时的时间戳.
    final long createTime = System.currentTimeMillis();
    buf.putLong(createTime);
    // 消息最大保留时间10秒,否则消息无效.
    buf.putInt(msg.getTimeout());
    // 写body长度和内容.
    if (null != body) {
      buf.putInt(bodyLength);
      buf.put(bodyBytes);
    } else {
      buf.putInt(0);
    }
    // 设置附件消息的长度.
    buf.putInt(attachmentSize);
    // 遍历附件消息.
    for (final Map.Entry<String, Object> entry : attachment.entrySet()) {
      final String key1 = entry.getKey();
      final String value = String.valueOf(entry.getValue());
      // 设置附件消息的key长度和内容.
      if (!Objects.isNull(key1)) {
        // 使用UTF-8编码字符串.
        final byte[] keyBytes = key1.getBytes(StandardCharsets.UTF_8);
        buf.putInt(keyBytes.length);
        buf.put(keyBytes);
      }
      // 设置附件消息的value长度和内容.
      if (!Objects.isNull(value)) {
        // 使用UTF-8编码字符串.
        final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        buf.putInt(valueBytes.length);
        buf.put(valueBytes);
      }
    }
    // 写自定义消息长度和内容.
    buf.putInt(customMsgLength);
    //
    buf.put(bytes);
    // 消息的最后一个字节是大写的Y.
    buf.put(Constants.END);
    return buf;
  }
}
