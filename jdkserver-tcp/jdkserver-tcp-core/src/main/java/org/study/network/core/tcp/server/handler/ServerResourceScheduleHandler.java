package org.study.network.core.tcp.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.study.core.context.WorkerContext;
import org.study.network.core.tcp.server.rpc.base.RpcServer;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-27 18:20
 * @since 2021-02-27 18:20:00
 */
@Sharable
public class ServerResourceScheduleHandler extends ChannelDuplexHandler {
  /** . */
  private static final Logger LOG = LogManager.getLogger(ServerResourceScheduleHandler.class);

  private ScheduledFuture<?> scheduledFuture;

  private final WorkerContext scheduleContext;

  private final RpcServer rpcServer;

  public ServerResourceScheduleHandler(
      final WorkerContext scheduleContext, final RpcServer rpcServer) {
    this.scheduleContext = scheduleContext;
    this.rpcServer = rpcServer;
  }

  private final class MonitoringTask implements Runnable {
    private ChannelHandlerContext ctx;

    public MonitoringTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      LOG.info("Server resource: ---> {}", rpcServer.getServerResourceManager());
    }
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    ScheduledExecutorService executorService = scheduleContext.getScheduledExecutorService();
    scheduledFuture =
        executorService.scheduleAtFixedRate(
            new MonitoringTask(ctx), 15000, 15000, TimeUnit.MILLISECONDS);
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
