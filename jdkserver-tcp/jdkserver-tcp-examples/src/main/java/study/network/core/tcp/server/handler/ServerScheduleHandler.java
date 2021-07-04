package study.network.core.tcp.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import study.core.context.WorkerContext;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-27 18:20
 * @since 2021-02-27 18:20:00
 */
public class ServerScheduleHandler extends ChannelDuplexHandler {
  private final WorkerContext scheduleContext;
  private ScheduledFuture<?> scheduledFuture;

  public ServerScheduleHandler(final WorkerContext scheduleContext) {
    this.scheduleContext = scheduleContext;
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

  private static final class TrafficMonitoringTask implements Runnable {
    private final ChannelHandlerContext ctx;

    public TrafficMonitoringTask(final ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      GlobalTrafficShapingHandler globalTrafficShapingHandler =
          (GlobalTrafficShapingHandler) ctx.pipeline().get("GlobalTrafficShapingHandler");
      // LOG.info("server global Traffic Shaping: ---> {}", globalTrafficShapingHandler);
      ChannelTrafficShapingHandler channelTrafficShapingHandler =
          (ChannelTrafficShapingHandler) ctx.pipeline().get("ChannelTrafficShapingHandler");
      // LOG.info("server channel Traffic Shaping: ---> {}", channelTrafficShapingHandler);
    }
  }
}
