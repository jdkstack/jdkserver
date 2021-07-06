package org.jdkstack.jdkserver.tcp.core.core.codecs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jdkstack.jdkserver.tcp.core.api.core.codecs.Message;
import org.jdkstack.jdkserver.tcp.core.api.core.handler.ChannelHandlerContext;
import org.jdkstack.jdkserver.tcp.core.ssl.StudyException;

/**
 * 解码器.
 *
 * <p>将ByteBuffer对象转换成Message消息对象.
 *
 * @author admin
 */
public class NetworkByteToMessageDecoderHandler
    extends AbstractByteToMessageDecoderHandler<Message> {

  /**
   * 将ByteBuffer解码成Message.
   *
   * <p>解码规则
   *
   * @param ctx 上下文.
   * @param body 从channel读取到的字节buffer.
   * @exception Exception 抛出所有异常.
   * @author admin
   */
  @Override
  protected Message decode(final ChannelHandlerContext ctx, final ByteBuffer body)
      throws Exception {
    // 按照编码规则进行解码.
    Message message = new NetworkMessage();
    // 报文第一个字节必须是83.
    byte start = body.get();
    if (start != Constants.START) {
      throw new StudyException("报文第一个字节不匹配" + Constants.START);
    }
    // CrcCode
    int crcCode = body.getInt();
    message.setCrcCode(crcCode);
    // Length
    int length = body.getInt();
    message.setLength(length);
    // SessionId
    long sessionId = body.getLong();
    message.setSessionId(sessionId);
    // Type
    int type = body.getInt();
    message.setType(type);
    // Priority
    int priority = body.getInt();
    message.setPriority(priority);
    // createTime
    long createTime = body.getLong();
    message.setCreateTime(createTime);
    // Timeout
    int timeout = body.getInt();
    message.setTimeout(timeout);
    // bodyLength
    int bodyLength = body.getInt();
    if (bodyLength > 0) {
      // body
      byte[] bodyBytes = new byte[bodyLength];
      body.get(bodyBytes);
      message.setBody(new String(bodyBytes, StandardCharsets.UTF_8));
      message.setLength(body.capacity() + 4);
    }
    // attachmentSize
    int attachmentSize = body.getInt();
    for (int i = 0; i < attachmentSize; i++) {
      int keyLen = body.getInt();
      byte[] key = new byte[keyLen];
      body.get(key);
      int valueLen = body.getInt();
      byte[] value = new byte[valueLen];
      body.get(value);
      message.attachment(
          new String(key, StandardCharsets.UTF_8), new String(value, StandardCharsets.UTF_8));
    }
    // customMsgLength
    int customMsgLength = body.getInt();
    // customMsg
    byte[] customMsg = new byte[customMsgLength];
    body.get(customMsg);
    // 报文最后一个字节必须是89.
    byte end = body.get();
    if (end != Constants.END) {
      throw new StudyException("报文最后一个字节不匹配" + Constants.END);
    }
    return message;
  }
}
