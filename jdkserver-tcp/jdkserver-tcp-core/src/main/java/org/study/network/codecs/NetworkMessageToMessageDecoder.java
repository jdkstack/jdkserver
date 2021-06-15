package org.study.network.codecs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自定义消息解码器,从一种消息转到另一种消息.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class NetworkMessageToMessageDecoder extends MessageToMessageDecoder<Map<String, String>> {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  protected void decode(
      final ChannelHandlerContext ctx, final Map<String, String> msg, final List<Object> out)
      throws Exception {
    String body = msg.get("body");
    // 继续将消息转换成List.
    List<String> message = new ArrayList<>(16);
    message.add(body);
    // 这样在channelRead方法中读取的时候,可以将对象转换成List.
    out.add(message);
  }
}
