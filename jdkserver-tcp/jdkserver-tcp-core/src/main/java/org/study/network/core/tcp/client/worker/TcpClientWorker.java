package org.study.network.core.tcp.client.worker;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.AttributeKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.study.core.context.WorkerContext;
import org.study.core.future.Handler;
import org.study.network.codecs.NetworkByteToMessageDecoder;
import org.study.network.codecs.NetworkMessageToByteEncoder;
import org.study.network.core.socket.NetSocket;
import org.study.network.core.socket.NetSocketImpl;
import org.study.network.core.tcp.client.handler.StudyClientHandler;
import org.study.network.core.tcp.client.rpc.base.RpcClient;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-20 17:29
 * @since 2021-01-20 17:29:00
 */
public class TcpClientWorker implements Handler<Channel> {
  /** 客户端读写数据处理器. */
  private final Handler<NetSocket> connectionHandler;
  /** 客户端读写数据异常处理器. */
  private final Handler<Throwable> exceptionHandler;
  /** 执行任务的上下文对象. */
  private final WorkerContext context;
  /** 自定义的处理器. */
  private final Map<String, ChannelHandler> channelHandlers = new LinkedHashMap<>(16);
  /** 全局流量限制对象. */
  private final GlobalTrafficShapingHandler trafficHandler;

  private final RpcClient rpcClient;

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param connectionHandler 连接处理器.
   * @param exceptionHandler 异常处理器.
   * @param context 上下午.
   * @author admin
   */
  public TcpClientWorker(
      final Handler<NetSocket> connectionHandler,
      final Handler<Throwable> exceptionHandler,
      final WorkerContext context,
      final RpcClient rpcClient) {
    this.connectionHandler = connectionHandler;
    this.exceptionHandler = exceptionHandler;
    this.context = context;
    this.rpcClient = rpcClient;
    this.trafficHandler =
        new GlobalTrafficShapingHandler(
            context.getScheduledExecutorService(), 10 * 1024 * 1024L, 20 * 1024 * 1024L, 1000L);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param channelHandler 业务处理器.
   * @param key 业务处理器key.
   * @author admin
   */
  public void add(final String key, final ChannelHandler channelHandler) {
    if (null != this.channelHandlers.get(key)) {
      throw new RuntimeException("已经存在同名的channelHandler.");
    }
    this.channelHandlers.put(key, channelHandler);
  }

  /**
   * 客户端SSL初始化.
   *
   * <p>Another description after blank line.
   *
   * @param ch channel对象.
   * @author admin
   */
  @Override
  public void handleSsl(final Channel ch) {
    try {
      final AttributeKey<String> host = AttributeKey.valueOf("host");
      final String h = ch.attr(host).get();
      final AttributeKey<Integer> port = AttributeKey.valueOf("port");
      final int po = ch.attr(port).get();
      final AttributeKey<Boolean> domainSocket = AttributeKey.valueOf("domainSocket");
      final boolean d = ch.attr(domainSocket).get();
      final AttributeKey<String> clientName = AttributeKey.valueOf("clientName");
      final String c = ch.attr(clientName).get();
      final AttributeKey<String> serverName = AttributeKey.valueOf("serverName");
      final String s = ch.attr(serverName).get();
      final ChannelPipeline pipeline = ch.pipeline();
      // final SslHandler ssl = KeystoreManager.createClientSniHandler(h, po, d, c, s);
      // SslHandler ssl = KeystoreManager.createClientSslHandler();
      // ssl.setHandshakeTimeout(10, TimeUnit.HOURS);
      // pipeline.addLast("ssl", ssl);
      /*final ChannelPromise p = ch.newPromise();
      //pipeline.addLast("ClientSslHandshakeHandler", new ClientSslHandshakeHandler(p));
      p.addListener(
          future -> {
            if (!future.isSuccess()) {
              this.handleException(future.cause());
            }
            // 握手成功(双向证书互相发送后,验证成功).
            if (future.isSuccess()) {
              //LOG.info("Client init ssl successful.");
            }
          });*/
    } catch (final Exception e) {
      // LOG.info("Client Handler Exception:{} ", e.getMessage());
    }
  }

  /**
   * 客户端和服务器SSL建立成功后,初始化业务处理器.
   *
   * <p>Another description after blank line.
   *
   * @param ch channel对象.
   * @author admin
   */
  @Override
  public void handle(final Channel ch) {
    try {
      final ChannelPipeline pipeline = ch.pipeline();
      // pipeline.addLast("GlobalTrafficShapingHandler", trafficHandler);
      final ChannelTrafficShapingHandler channelTrafficShapingHandler =
          new ChannelTrafficShapingHandler(512 * 1024L, 256 * 1024L);
      // pipeline.addLast("ChannelTrafficShapingHandler", channelTrafficShapingHandler);
      // 客户端调度处理器,专门用来处理,调度任务.
      // pipeline.addLast("ClientScheduleHandler", new ClientScheduleHandler(context));
      // 添加通用的handler.
      pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
      // 添加自定义的handler.
      pipeline.addLast("Decoder", new NetworkByteToMessageDecoder());
      pipeline.addLast("Encoder", new NetworkMessageToByteEncoder());
      // 客户端发起登陆请求,同时处理服务器端登陆响应.
      // pipeline.addLast("LoginAuthReqHandler", new ClientLoginAuthReqHandler());
      // 处理服务器端发来的心跳,并响应服务器端的心跳.
      // pipeline.addLast("HeartBeatHandler", new ClientHeartBeatRespHandler());
      // 当服务器端登陆成功后,向客户端发送登陆响应消息,客户端处理这个请求,客户端发起心跳请求.
      // pipeline.addLast("HeartBeatReqHandler", new ClientHeartBeatReqHandler(context));
      // pipeline.addLast(
      //   "ClientResourceScheduleHandler", new ClientResourceScheduleHandler(context, rpcClient));
      // 读写多少次刷新处理器handler.
      // pipeline.addLast("flushConsolidationHandler", new FlushConsolidationHandler(1024, true));
      // 日志处理器handler.
      // pipeline.addLast("logging", new LoggingHandler());
      // 读写超时处理器handler.
      pipeline.addLast("idle", new IdleStateHandler(0, 0, 30000, TimeUnit.SECONDS));
      // 业务处理器handler.
      final StudyClientHandler<NetSocketImpl> nh =
          StudyClientHandler.create(ctx -> new NetSocketImpl(ctx, context));
      nh.addHandler(connectionHandler::handle);
      pipeline.addLast("handler", nh);
    } catch (final Exception e) {
      // LOG.info("Client Handler Exception:{} ", e.getMessage());
    }
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @param cause 异常.
   * @author admin
   */
  private void handleException(final Throwable cause) {
    if (this.exceptionHandler != null) {
      this.context.executeInExecutorService(cause, exceptionHandler);
    }
  }
}
