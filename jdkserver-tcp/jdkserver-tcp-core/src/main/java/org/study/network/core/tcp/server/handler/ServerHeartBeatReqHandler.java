package org.study.network.core.tcp.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.study.core.context.WorkerContext;
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
public class ServerHeartBeatReqHandler extends ChannelInboundHandlerAdapter {
  /** . */
  private static final Logger LOG = LogManager.getLogger(ServerHeartBeatReqHandler.class);
  private final WorkerContext scheduleContext;
  private ScheduledFuture<?> scheduledFuture;

  public ServerHeartBeatReqHandler(final WorkerContext scheduleContext) {
    this.scheduleContext = scheduleContext;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

    super.handlerAdded(ctx);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (null != this.scheduledFuture) {
      this.scheduledFuture.cancel(true);
      this.scheduledFuture = null;
    }
    super.handlerRemoved(ctx);
  }

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
      // 握手成功，主动发送心跳消息
      final int type = message.getType();
      if (type == NetworkMessageType.PING.value()) {
        if (this.scheduledFuture == null) {
          ScheduledExecutorService scheduledExecutorService =
              scheduleContext.getScheduledExecutorService();
          this.scheduledFuture =
              scheduledExecutorService.scheduleAtFixedRate(
                  new HeartBeatTask(ctx), 5000, 5000, TimeUnit.MILLISECONDS);
        }
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
  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
      throws Exception {
    if (null != this.scheduledFuture) {
      this.scheduledFuture.cancel(true);
      this.scheduledFuture = null;
    }
    ctx.fireExceptionCaught(cause);
    cause.printStackTrace();
  }

  public static class HeartBeatTask implements Runnable {
    /** . */
    private final ChannelHandlerContext ctx;

    /**
     * This is a method description.
     *
     * <p>Another description after blank line.
     *
     * @author admin
     */
    public HeartBeatTask(final ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    /**
     * This is a method description.
     *
     * <p>Another description after blank line.
     *
     * @author admin
     */
    @Override
    public void run() {
      final Message heatBeat = buildHeatBeat();
      LOG.info("server send heart beat messsage to Client : ---> {}", heatBeat);
      this.ctx.writeAndFlush(heatBeat);
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
      message.setType(NetworkMessageType.PING.value());
      return message;
    }
  }
}