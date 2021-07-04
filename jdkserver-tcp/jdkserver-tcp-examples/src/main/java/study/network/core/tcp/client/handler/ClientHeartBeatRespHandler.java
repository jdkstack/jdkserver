package study.network.core.tcp.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import study.network.codecs.Message;
import study.network.codecs.NetworkMessage;
import study.network.codecs.NetworkMessageType;

/**
 * This is a method description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 */
public class ClientHeartBeatRespHandler extends ChannelInboundHandlerAdapter {

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    if (msg instanceof Message) {
      final Message message = (Message) msg;
      // 返回心跳应答消息
      if (message.getType() == NetworkMessageType.PING.value()) {
        // LOG.info("Client Receive server heart beat message : ---> {}", message);
        final Message heartBeat = buildHeatBeat();
        // LOG.info("Client Send heart beat response message to server: ---> {}", heartBeat);
        ctx.writeAndFlush(heartBeat);
        ctx.fireChannelRead(msg);
      } else {
        ctx.fireChannelRead(msg);
      }
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private Message buildHeatBeat() {
    final Message message = new NetworkMessage();
    message.setType(NetworkMessageType.PONG.value());
    return message;
  }
}
