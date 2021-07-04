package study.network.core.tcp.server.worker;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import study.core.context.WorkerContext;
import study.core.future.Handler;
import study.network.codecs.NetworkByteToMessageDecoder;
import study.network.codecs.NetworkMessageToByteEncoder;
import study.network.core.common.pool.Connection;
import study.network.core.socket.NetSocket;
import study.network.core.socket.NetSocketImpl;
import study.network.core.tcp.client.pool.TcpClientConnection;
import study.network.core.tcp.server.handler.ServerLoginAuthRespHandler;
import study.network.core.tcp.server.handler.StudyServerHandler;
import study.network.core.tcp.server.manager.ServerConnectionPoolManager;
import study.network.core.tcp.server.rpc.base.RpcServer;
import study.network.core.tool.RemotingUtil;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2020-12-29 13:19
 * @since 2020-12-29 13:19:00
 */
public class TcpServerWorker implements Handler<Channel> {
  /** 有序添加和访问元素. */
  private final Handler<NetSocket> connectionHandler;
  /** 有序添加和访问元素. */
  private final Handler<Throwable> exceptionHandler;
  /** 有序添加和访问元素. */
  private final WorkerContext context;
  /** 有序添加和访问元素. */
  private final Map<String, ChannelHandler> channelHandlers = new LinkedHashMap<>(16);
  /** 有序添加和访问元素. */
  private final GlobalTrafficShapingHandler trafficHandler;

  private final RpcServer rpcServer;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public TcpServerWorker(
      final Handler<NetSocket> connectionHandler,
      final Handler<Throwable> exceptionHandler,
      final WorkerContext context,
      final RpcServer rpcServer) {
    this.connectionHandler = connectionHandler;
    this.exceptionHandler = exceptionHandler;
    this.context = context;
    this.rpcServer = rpcServer;
    this.trafficHandler =
        new GlobalTrafficShapingHandler(
            context.getScheduledExecutorService(), 10 * 1024 * 1024L, 20 * 1024 * 1024L);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public void add(final String key, final ChannelHandler channelHandler) {
    if (null != this.channelHandlers.get(key)) {
      throw new RuntimeException("已经存在同名的channelHandler.");
    }
    this.channelHandlers.put(key, channelHandler);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void handle(final Channel ch) {
    try {
      final ChannelPipeline pipeline = ch.pipeline();
      // 将客户端的Channel存储起来.
      extracted(ch);
      // 创建服务器端的SNI处理器.
      // final SniHandler sni = KeystoreManager.createServerSniHandler("www.server2.com");
      // pipeline.addLast("sni", sni);
      // final SslHandler ssl = KeystoreManager.createServerSslHandler();
      // ssl.setHandshakeTimeout(10, TimeUnit.HOURS);
      // pipeline.addLast("ssl", ssl);
      this.handleSuccess(ch.pipeline());
      // 异步Promise.
      /*final ChannelPromise p = ch.newPromise();
      // 创建服务器端SSL(SNI)握手处理器.
      //pipeline.addLast("ServerSslHandshakeHandler", new ServerSslHandshakeHandler(p));
      // 等待SSL(SNI)握手完成.
      p.addListener(
          future -> {
            // 握手失败(双向证书互相发送后,验证失败).
            if (!future.isSuccess()) {
              this.handleException(future.cause());
            }
            // 握手成功(双向证书互相发送后,验证成功).
            if (future.isSuccess()) {
              this.handleSuccess(ch.pipeline());
            }
          });*/
    } catch (final Exception e) {
      // LOG.info("Server Handler Exception:{} ", e.getMessage());
    }
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private void handleSuccess(final ChannelPipeline pipeline) {
    try {
      final int flush = 1024;
      final int read = 0;
      final int write = 0;
      final int allIdleTime = 30000;
      // 整个server的流量限制.
      // pipeline.addLast("GlobalTrafficShapingHandler", trafficHandler);
      // 单个channel的流量限制.
      // pipeline.addLast(
      //    "ChannelTrafficShapingHandler",
      //    new ChannelTrafficShapingHandler(1024 * 1024L, 2 * 1024 * 1024L));
      // 服务端调度处理器,专门用来处理,调度任务.
      // pipeline.addLast("ServerScheduleHandler", new ServerScheduleHandler(context));
      // 添加通用的handler.
      pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
      // 添加自定义的解码器.
      pipeline.addLast("Decoder", new NetworkByteToMessageDecoder());
      // 添加自定义的编码器.
      pipeline.addLast("Encoder", new NetworkMessageToByteEncoder());
      // 添加登陆响应处理器.
      pipeline.addLast("LoginAuthRespHandler", new ServerLoginAuthRespHandler());
      // 添加心跳响应处理器.
      // pipeline.addLast("HeartBeatRespHandler", new ServerHeartBeatRespHandler());
      // 添加心跳请求处理器.
      // pipeline.addLast("HeartBeatHandler", new ServerHeartBeatReqHandler(context));
      // pipeline.addLast(
      //    "ServerResourceScheduleHandler", new ServerResourceScheduleHandler(context, rpcServer));
      // 刷新处理器,读写数据多少次时才Flush一次.
      // pipeline.addLast("flush", new FlushConsolidationHandler(flush, true));
      // 日志处理器.
      // pipeline.addLast("logging", new LoggingHandler());
      // 读写超时处理器.
      pipeline.addLast("idle", new IdleStateHandler(read, write, allIdleTime, TimeUnit.SECONDS));
      // 业务处理器.
      final StudyServerHandler<NetSocketImpl> nh =
          StudyServerHandler.create(channel -> new NetSocketImpl(channel, context));
      nh.addHandler(connectionHandler::handle);
      pipeline.addLast("handler", nh);
    } catch (final Exception e) {
      // LOG.info("Server Handler Exception:{} ", e.getMessage());
    }
  }

  /**
   * 服务器和客户端建立连接以后,保存channel对象.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private void extracted(final Channel ch) {
    final ServerConnectionPoolManager serverConnectionPoolManager =
        ServerConnectionPoolManager.getInstance();
    final String s = RemotingUtil.parseRemoteAddress(ch);
    final String[] split = s.split(":");
    final Connection connection = new TcpClientConnection(ch);
    serverConnectionPoolManager.createTcpClientConnection(split[0], connection);
    // LOG.info("Server Connection Pool Manager:{} ", connection);
  }

  /**
   * 异常处理.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private void handleException(final Throwable cause) {
    if (this.exceptionHandler != null) {
      this.context.executeInExecutorService(cause, exceptionHandler);
    }
  }
}
