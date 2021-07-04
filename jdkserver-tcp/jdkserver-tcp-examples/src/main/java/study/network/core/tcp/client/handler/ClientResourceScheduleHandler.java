package study.network.core.tcp.client.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import study.core.context.WorkerContext;
import study.network.core.tcp.client.rpc.base.RpcClient;

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
public class ClientResourceScheduleHandler extends ChannelDuplexHandler {

  private final WorkerContext scheduleContext;
  private final RpcClient rpcClient;
  private ScheduledFuture<?> scheduledFuture;

  public ClientResourceScheduleHandler(
      final WorkerContext scheduleContext, final RpcClient rpcClient) {
    this.scheduleContext = scheduleContext;
    this.rpcClient = rpcClient;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    ScheduledExecutorService executorService = scheduleContext.getScheduledExecutorService();
    scheduledFuture =
        executorService.scheduleAtFixedRate(
            new MonitoringTask(ctx), 5000, 5000, TimeUnit.MILLISECONDS);
    super.handlerAdded(ctx);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
    }
    super.handlerRemoved(ctx);
  }

  private final class MonitoringTask implements Runnable {
    private ChannelHandlerContext ctx;

    public MonitoringTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public void run() {
      // LOG.info("Client resource: ---> {}", rpcClient);
    }
  }
}
