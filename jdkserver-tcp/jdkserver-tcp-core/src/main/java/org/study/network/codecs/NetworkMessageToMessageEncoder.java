package org.study.network.codecs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网络消息消息编码器.
 *
 * @author admin
 */
public class NetworkMessageToMessageEncoder extends MessageToMessageEncoder<NetworkMessage> {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  protected void encode(
      final ChannelHandlerContext ctx, final NetworkMessage msg, final List<Object> out)
      throws Exception {
    // 得到消息体对象.
    String body = msg.getBody();
    // 将解码的字符串内容放入List中.
    Map<String, String> message = new HashMap<>(Constants.INITIAL_CAPACITY);
    message.put("body", body);
    out.add(message);
  }
}
