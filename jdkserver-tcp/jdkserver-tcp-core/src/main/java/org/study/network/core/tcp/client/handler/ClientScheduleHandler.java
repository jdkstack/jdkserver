package org.study.network.core.tcp.client.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.study.core.context.WorkerContext;
import org.study.core.context.WorkerStudyContextImpl;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-27 18:20
 * @since 2021-02-27 18:20:00
 */
public class ClientScheduleHandler extends ChannelDuplexHandler {

  private ScheduledFuture<?> scheduledFuture;

  private final WorkerContext scheduleContext;

  public ClientScheduleHandler(final WorkerContext scheduleContext) {
    this.scheduleContext = scheduleContext;
  }

  private final class TrafficMonitoringTask implements Runnable {
    private ChannelHandlerContext ctx;

    public TrafficMonitoringTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      GlobalTrafficShapingHandler globalTrafficShapingHandler =
          (GlobalTrafficShapingHandler) ctx.pipeline().get("GlobalTrafficShapingHandler");
      //LOG.info("Client global Traffic Shaping: ---> {}", globalTrafficShapingHandler);
      ChannelTrafficShapingHandler channelTrafficShapingHandler =
          (ChannelTrafficShapingHandler) ctx.pipeline().get("ChannelTrafficShapingHandler");
      //LOG.info("Client channel Traffic Shaping: ---> {}", channelTrafficShapingHandler);
    }
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    ScheduledExecutorService executorService = scheduleContext.getScheduledExecutorService();
    scheduledFuture =
        executorService.scheduleAtFixedRate(
            new TrafficMonitoringTask(ctx), 5000, 5000, TimeUnit.MILLISECONDS);
    super.handlerAdded(ctx);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
    super.handlerRemoved(ctx);
  }
}
