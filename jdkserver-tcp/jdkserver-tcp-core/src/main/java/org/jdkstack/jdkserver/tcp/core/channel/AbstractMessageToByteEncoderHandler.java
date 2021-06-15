package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.study.network.codecs.Message;

public abstract class AbstractMessageToByteEncoderHandler<T> implements ChannelOutboundHandler {

  public void write(ChannelHandlerContext ctx, Message msg) throws Exception {
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
      final String key1 = entry.getKey();
      final String value = String.valueOf(entry.getValue());
      // 设置附件消息的key长度和内容.
      if (!Objects.isNull(key1)) {
        // 使用UTF-8编码字符串.
        final byte[] keyBytes = key1.getBytes(StandardCharsets.UTF_8);
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
    int totalLength =
        headerLength
            + bodyLength
            + attachmentLength
            + customMsgLength
            + 1
            + 12
            + attachmentSize * 4 * 2;
    ByteBuffer buf = ByteBuffer.allocate(totalLength);
    buf.putInt(totalLength);
    try {
      encode(ctx, msg, buf);
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

  protected abstract void encode(ChannelHandlerContext ctx, Message msg, ByteBuffer out)
      throws Exception;
}
