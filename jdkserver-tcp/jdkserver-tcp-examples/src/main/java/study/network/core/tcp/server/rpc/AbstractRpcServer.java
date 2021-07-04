package study.network.core.tcp.server.rpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import study.core.context.Monitor;
import study.core.context.StudyThreadFactory;
import study.core.context.ThreadMonitor;
import study.core.context.WorkerContext;
import study.core.context.WorkerStudyContextImpl;
import study.core.future.Handler;
import study.network.core.common.keysore.KeystoreManager;
import study.network.core.common.manager.AbstractEventLoopGroupManager;
import study.network.core.common.option.Constants;
import study.network.core.common.option.KeystoreOptions;
import study.network.core.common.option.TrustKeystoreOptions;
import study.network.core.common.pool.Connection;
import study.network.core.socket.NetSocket;
import study.network.core.tcp.server.base.TcpServer;
import study.network.core.tcp.server.base.TcpServerImpl;
import study.network.core.tcp.server.handler.ServerExceptionHandler;
import study.network.core.tcp.server.handler.ServerReadWriteHandler;
import study.network.core.tcp.server.initializer.ServerLoadBalanceChannelInitializer;
import study.network.core.tcp.server.manager.ServerConnectionPoolManager;
import study.network.core.tcp.server.manager.ServerResourceManager;
import study.network.core.tcp.server.option.RpcServerOptions;
import study.network.core.tcp.server.option.TcpServerOptions;
import study.network.core.tcp.server.pool.TcpServerConnection;
import study.network.core.tcp.server.rpc.base.RpcServer;
import study.network.core.tcp.server.worker.TcpServerWorker;
import study.network.core.tool.RemotingUtil;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 15:23
 * @since 2021-03-03 15:23:00
 */
public abstract class AbstractRpcServer implements RpcServer {
  /** 用来存储服务器端的所有资源. */
  protected static final ServerResourceManager SERVER_RESOURCE_MANAGER =
      new ServerResourceManager();
  /** 用来存储服务器端的所有连接池. */
  protected static final ServerConnectionPoolManager SERVER_CONNECTION_POOL_MANAGER =
      ServerConnectionPoolManager.getInstance();
  /** 监控服务端的所有自定义线程.不包括引入Jar内部的线程,以及JDK内部线程. */
  protected static final Monitor checker = new ThreadMonitor(Constants.BLOCK_TIME);
  /** 服务器端的定时调度线程池. */
  protected static final ScheduledExecutorService scheduledExecutorService =
      new ScheduledThreadPoolExecutor(
          Constants.CORE_POOL_SIZE, new StudyThreadFactory(Constants.STUDY_SCHEDULED, null));
  /** 给Netty使用的线程工厂. */
  protected static final ThreadFactory masterThreadFactory =
      new StudyThreadFactory(Constants.STUDY_MASTER, checker);
  /** 给Netty使用的线程工厂. */
  protected static final ThreadFactory studyThreadFactory =
      new StudyThreadFactory(Constants.STUDY_, checker);
  /** 线程池,用于处理非Netty的任务. */
  protected static final ExecutorService executorService =
      new ThreadPoolExecutor(
          Constants.CORE_POOL_SIZE2,
          Constants.MAXIMUM_POOL_SIZE,
          Constants.KEEP_ALIVE_TIME,
          TimeUnit.MILLISECONDS,
          new LinkedTransferQueue<>(),
          new StudyThreadFactory(Constants.STUDY_EXECUTE_BLOCK, checker));
  /** 工作任务上下文. */
  protected static final WorkerContext context =
      new WorkerStudyContextImpl(executorService, scheduledExecutorService);
  /** . */
  private static final Logger LOG = LogManager.getLogger(AbstractRpcServer.class);

  static {
    // 使用Netty自身的日志.
    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    // 用于做内存泄露的检测,Netty内置的检测功能.生产环境默认禁用.
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
    // 使用jdk默认的编码解码器.
    System.setProperty(Constants.NO_JDK_ZLIB_DECODER, Constants.FALSE);
    System.setProperty(Constants.NO_JDK_ZLIB_Encoder, Constants.FALSE);
    // 线程监控任务.
    checker.monitor(context);
    KeystoreOptions keystoreOptions = new KeystoreOptions("xxxxxx", "PKCS12", "/cert/server");
    TrustKeystoreOptions trustKeystoreOptions =
        new TrustKeystoreOptions("xxxxxx", "PKCS12", "/cert/server-t");
    try {
      KeystoreManager.parserKeystore(keystoreOptions);
      KeystoreManager.parserTrustKeystore(trustKeystoreOptions);
      KeystoreManager.initServerContext();
    } catch (Exception e) {
      LOG.info("Server init ssl Exception : {}.", e.getMessage());
    }
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected static void handleThrowable(final Throwable cause) {

    LOG.info("Failed to bind:{}.", cause.getMessage());
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected static void handleChannel(final Channel channel) {
    final String s = RemotingUtil.parseLocalAddress(channel);
    final String[] split = s.split(":");
    final String id = split[0] + ":" + split[1];
    final Connection connection = new TcpServerConnection(channel);
    SERVER_CONNECTION_POOL_MANAGER.createTcpServerConnection(id, connection);
    LOG.info("Server is now listening on :{}.", s);
  }

  protected void init(RpcServerOptions rpcServerOptions) {
    String localId = rpcServerOptions.getLocalHost() + ":" + rpcServerOptions.getLocalPort();
    AbstractEventLoopGroupManager study = new AbstractEventLoopGroupManager(studyThreadFactory);
    AbstractEventLoopGroupManager master = new AbstractEventLoopGroupManager(masterThreadFactory);
    EventLoopGroup studyEventLoopGroup =
        study.createEventLoopGroup(50, TcpServerOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
    List<EventLoopGroup> eventLoopGroups = new ArrayList<>();
    eventLoopGroups.add(studyEventLoopGroup);
    EventLoopGroup masterEventLoopGroup = master.createEventLoopGroup(100, 1);
    eventLoopGroups.add(masterEventLoopGroup);
    SERVER_RESOURCE_MANAGER.eventLoopGroups.put(localId, eventLoopGroups);
  }

  protected void initServer(RpcServerOptions rpcServerOptions) {
    String localHost = rpcServerOptions.getLocalHost();
    int localPort = rpcServerOptions.getLocalPort();
    String localId = localHost + ":" + localPort;
    Handler<NetSocket> connectionHandler = new ServerReadWriteHandler();
    Handler<Throwable> exceptionHandler = new ServerExceptionHandler();
    TcpServerWorker tcpServerWorker =
        new TcpServerWorker(connectionHandler, exceptionHandler, context, this);
    TcpServerOptions tcpServerOptions = new TcpServerOptions();
    TcpServer tcpServer = new TcpServerImpl(tcpServerOptions);
    int workerCount = 10;
    ServerLoadBalanceChannelInitializer workerChannelBalance =
        new ServerLoadBalanceChannelInitializer();
    SERVER_RESOURCE_MANAGER.lb.put(localId, workerChannelBalance);
    List<EventLoopGroup> eventLoopGroups = SERVER_RESOURCE_MANAGER.eventLoopGroups.get(localId);
    EventLoopGroup studyEventLoopGroup = eventLoopGroups.get(0);
    EventLoopGroup masterEventLoopGroup = eventLoopGroups.get(1);
    for (int j = 0; j < workerCount; j++) {
      EventLoop eventLoop = studyEventLoopGroup.next();
      workerChannelBalance.addWorker(eventLoop, tcpServerWorker);
    }
    tcpServer.initTcpServer(workerChannelBalance, masterEventLoopGroup);
    SERVER_RESOURCE_MANAGER.connectionPoolManagers.put(localId, SERVER_CONNECTION_POOL_MANAGER);
    ChannelFuture cf = tcpServer.listenServer(localPort, localHost);
    cf.addListener(
        res -> {
          if (res.isSuccess()) {
            Channel channel = cf.channel();
            handleChannel(channel);
          } else {
            Throwable cause = res.cause();
            handleThrowable(cause);
          }
        });
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public ServerResourceManager getServerResourceManager() {
    return SERVER_RESOURCE_MANAGER;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public void shutdown() {}
}
