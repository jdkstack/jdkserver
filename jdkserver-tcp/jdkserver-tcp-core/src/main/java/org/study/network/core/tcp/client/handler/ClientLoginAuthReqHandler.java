package org.study.network.core.tcp.client.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.study.network.codecs.Message;
import org.study.network.codecs.NetworkMessage;
import org.study.network.codecs.NetworkMessageType;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ClientLoginAuthReqHandler extends ChannelDuplexHandler {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.writeAndFlush(buildLoginReq());
    super.channelActive(ctx);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof Message) {
      NetworkMessage message = (NetworkMessage) msg;
      // 如果是握手应答消息，需要判断是否认证成功
      if (message.getType() == NetworkMessageType.LOGIN_RESPONSE.value()) {
        String body = message.getBody();
        byte aByte = body.getBytes(StandardCharsets.UTF_8)[0];
        if (aByte != (byte) '0') {
          // LOG.error("Login is not ok :{} ", message);
          // 登陆失败,关闭连接
          ctx.close();
        } else {
          // LOG.info("Login is ok :{} ", message);
        }
      }
    }
    ctx.fireChannelRead(msg);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private NetworkMessage buildLoginReq() {
    NetworkMessage message = new NetworkMessage();
    message.setPriority((byte) 0);
    message.setSessionId(UUID.randomUUID().getMostSignificantBits());
    message.setType(NetworkMessageType.LOGIN_REQUEST.value());
    message.setBody("{}");
    message.setLength(19999);
    return message;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.fireExceptionCaught(cause);
    cause.printStackTrace();
  }
}
