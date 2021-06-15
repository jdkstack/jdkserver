package org.study.network.core.tcp.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ServerHeartBeatRespHandler extends ChannelDuplexHandler {
  /** . */
  private static final Logger LOG = LogManager.getLogger(ServerHeartBeatRespHandler.class);

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
        LOG.info("Server Receive client heart beat message : ---> {}", message);
        final Message heartBeat = buildHeatBeat();
        LOG.info("Server Send heart beat response message to client : ---> {}", heartBeat);
        ctx.writeAndFlush(heartBeat);
      }
    }
    ctx.fireChannelRead(msg);
  }

  private Message buildHeatBeat() {
    final Message message = new NetworkMessage();
    message.setType(NetworkMessageType.PONG.value());
    return message;
  }
}
