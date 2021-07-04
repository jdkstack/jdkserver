package study.network.core.tcp.client.rpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import study.network.core.common.manager.AbstractConnectionPoolManager;
import study.network.core.common.manager.AbstractEventLoopGroupManager;
import study.network.core.common.manager.ConnectionPoolManager;
import study.network.core.common.option.Constants;
import study.network.core.common.option.KeystoreOptions;
import study.network.core.common.option.TrustKeystoreOptions;
import study.network.core.common.pool.AbstractConnection;
import study.network.core.common.pool.AbstractConnectionPool;
import study.network.core.common.pool.Connection;
import study.network.core.common.pool.ConnectionPool;
import study.network.core.socket.NetSocket;
import study.network.core.tcp.client.base.ConnectionEventType;
import study.network.core.tcp.client.base.TcpClientImpl;
import study.network.core.tcp.client.handler.ClientExceptionHandler;
import study.network.core.tcp.client.handler.ClientReadWriteHandler;
import study.network.core.tcp.client.initializer.ClientLoadBalanceChannelInitializer;
import study.network.core.tcp.client.manager.ClientConnectionPoolManager;
import study.network.core.tcp.client.manager.ClientResourceManager;
import study.network.core.tcp.client.option.RpcClientOptions;
import study.network.core.tcp.client.option.TcpClientOptions;
import study.network.core.tcp.client.pool.TcpClientConnection;
import study.network.core.tcp.client.rpc.base.RpcClient;
import study.network.core.tcp.client.worker.TcpClientWorker;
import study.network.core.tool.RemotingUtil;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 15:26
 * @since 2021-03-03 15:26:00
 */
public abstract class AbstractRpcClient implements RpcClient {
  /** . */
  protected static final ClientResourceManager CLIENT_RESOURCE_MANAGER =
      new ClientResourceManager();
  /** . */
  protected static final ClientConnectionPoolManager CLIENT_CONNECTION_POOL_MANAGER =
      ClientConnectionPoolManager.getInstance();
  /** 监控服务端的所有自定义线程.不包括引入Jar内部的线程,以及JDK内部线程. */
  protected static final Monitor CHECKER = new ThreadMonitor(Constants.BLOCK_TIME);
  /** 客户端端的定时调度线程池. */
  protected static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
      new ScheduledThreadPoolExecutor(
          Constants.CORE_POOL_SIZE, new StudyThreadFactory(Constants.STUDY_SCHEDULED, CHECKER));
  /** 客户端端的定时调度线程池. */
  protected static final ThreadFactory STUDY_THREAD_FACTORY =
      new StudyThreadFactory(Constants.STUDY_, CHECKER);
  /** . */
  protected static final ExecutorService EXECUTOR_SERVICE =
      new ThreadPoolExecutor(
          Constants.CORE_POOL_SIZE2,
          Constants.MAXIMUM_POOL_SIZE,
          Constants.KEEP_ALIVE_TIME,
          TimeUnit.MILLISECONDS,
          new LinkedTransferQueue<>(),
          new StudyThreadFactory(Constants.STUDY_EXECUTE_BLOCK, CHECKER));
  /** . */
  protected static final WorkerContext CONTEXT =
      new WorkerStudyContextImpl(EXECUTOR_SERVICE, SCHEDULED_EXECUTOR_SERVICE);
  /** . */
  private static final Logger LOG = LogManager.getLogger(AbstractRpcClient.class);

  static {
    // 使用Netty自身的日志.
    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    // 用于做内存泄露的检测,Netty内置的检测功能.生产环境默认禁用.
    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
    // 使用jdk默认的编码解码器.
    System.setProperty(Constants.NO_JDK_ZLIB_DECODER, Constants.FALSE);
    System.setProperty(Constants.NO_JDK_ZLIB_Encoder, Constants.FALSE);
    // 线程监控任务.
    CHECKER.monitor(CONTEXT);
    KeystoreOptions keystoreOptions = new KeystoreOptions("xxxxxx", "PKCS12", "/cert/client");
    TrustKeystoreOptions trustKeystoreOptions =
        new TrustKeystoreOptions("xxxxxx", "PKCS12", "/cert/client-t");
    try {
      KeystoreManager.parserKeystore(keystoreOptions);
      KeystoreManager.parserTrustKeystore(trustKeystoreOptions);
      KeystoreManager.initClientContext(null);
    } catch (Exception e) {
      LOG.info("Client init ssl Exception : {}.", e.getMessage());
    }
  }

  protected final List<Handler<NetSocket>> connectionHandlers = new ArrayList<>(16);

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected static void handleThrowable(Throwable e) {

    LOG.info("Failed to bind : {}.", e.getMessage());
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected static void handleChannel(Channel channel) {
    if (channel.isActive()) {
      channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
    } else {
      channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
    }
    String s1 = RemotingUtil.parseRemoteAddress(channel);
    String[] split1 = s1.split(":");
    String remoteId = split1[0] + ":" + split1[1];
    String s = RemotingUtil.parseLocalAddress(channel);
    // String[] split = s.split(":"); String localId = split[0] + ":" + split[1];
    Connection connection = new TcpClientConnection(channel);
    CLIENT_CONNECTION_POOL_MANAGER.createTcpClientConnection(remoteId, connection);
    ClientLoadBalanceChannelInitializer lb = CLIENT_RESOURCE_MANAGER.lb.get(remoteId);
    lb.init(channel);
    LOG.info("Client is now connecting to : {}.", s);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected void init(RpcClientOptions rpcClientOptions) {
    String remoteId = rpcClientOptions.getRemoteHost() + ":" + rpcClientOptions.getRemotePort();
    AbstractEventLoopGroupManager study = new AbstractEventLoopGroupManager(STUDY_THREAD_FACTORY);
    EventLoopGroup studyEventLoopGroup = study.createEventLoopGroup(50, 10);
    final List<EventLoopGroup> eventLoopGroups = new ArrayList<>();
    eventLoopGroups.add(studyEventLoopGroup);
    CLIENT_RESOURCE_MANAGER.eventLoopGroups.put(remoteId, eventLoopGroups);
    ClientLoadBalanceChannelInitializer workerChannelBalance =
        new ClientLoadBalanceChannelInitializer();
    CLIENT_RESOURCE_MANAGER.lb.put(remoteId, workerChannelBalance);

    final List<ExecutorService> executorServices = new ArrayList<>();
    executorServices.add(EXECUTOR_SERVICE);
    executorServices.add(SCHEDULED_EXECUTOR_SERVICE);
    CLIENT_RESOURCE_MANAGER.threadPoolExecutors.put(remoteId, executorServices);
    CLIENT_RESOURCE_MANAGER.connectionPoolManagers.put(remoteId, CLIENT_CONNECTION_POOL_MANAGER);
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  protected void initClient(RpcClientOptions rpcClientOptions) {
    String remoteHost = rpcClientOptions.getRemoteHost();
    int remotePort = rpcClientOptions.getRemotePort();
    String clientAlias = rpcClientOptions.getClientAlias();
    String serverAlias = rpcClientOptions.getServerAlias();
    String remoteId = remoteHost + ":" + remotePort;
    int count = rpcClientOptions.getCount();
    int localPort = rpcClientOptions.getLocalPort();
    String localHost = rpcClientOptions.getLocalHost();
    for (int j = 0; j < count; j++) {
      localPort = localPort + 1;
      // String localId = localHost + ":" + localPort;
      // 客户端连接成功后的数据读写处理.
      Handler<NetSocket> connectionHandler = new ClientReadWriteHandler();
      connectionHandlers.add(connectionHandler);
      // ssl初始化异常处理.
      final Handler<Throwable> exceptionHandler = new ClientExceptionHandler();

      final TcpClientWorker tcpClientWorker =
          new TcpClientWorker(connectionHandler, exceptionHandler, CONTEXT, this);
      final TcpClientOptions tcpClientOptions = new TcpClientOptions();
      final TcpClientImpl tcpClientServiceBean = new TcpClientImpl(tcpClientOptions);
      ClientLoadBalanceChannelInitializer workerChannelBalance =
          CLIENT_RESOURCE_MANAGER.lb.get(remoteId);
      final List<EventLoopGroup> eventLoopGroups =
          CLIENT_RESOURCE_MANAGER.eventLoopGroups.get(remoteId);
      EventLoopGroup studyEventLoopGroup = eventLoopGroups.get(0);
      final EventLoop eventLoop = studyEventLoopGroup.next();
      workerChannelBalance.addWorker(eventLoop, tcpClientWorker);
      workerChannelBalance.setRemoteHost(remoteHost);
      workerChannelBalance.setRemotePort(remotePort);
      workerChannelBalance.setClientName(clientAlias);
      workerChannelBalance.setServerName(serverAlias);
      tcpClientServiceBean.initTcpClient(workerChannelBalance);
      final ChannelFuture cf =
          tcpClientServiceBean.connectServer(remotePort, remoteHost, localPort, localHost);
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
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public ClientResourceManager getClientResourceManager() {
    return CLIENT_RESOURCE_MANAGER;
  }

  /**
   * This is a method description.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  @Override
  public String toString() {
    Set<Entry<String, ConnectionPoolManager>> entries =
        CLIENT_RESOURCE_MANAGER.connectionPoolManagers.entrySet();
    StringBuilder builder = new StringBuilder(290);
    for (Entry<String, ConnectionPoolManager> entry : entries) {
      String key = entry.getKey();
      builder.append("Client Connection Pool Manager Name: ").append(key);
      AbstractConnectionPoolManager value = (AbstractConnectionPoolManager) entry.getValue();
      Map<String, ConnectionPool> connectionPools = value.getConnectionPools();
      for (Entry<String, ConnectionPool> connectionPool : connectionPools.entrySet()) {
        String key1 = connectionPool.getKey();
        builder.append(" Client Connection Pool Name: ").append(key1);
        AbstractConnectionPool value1 = (AbstractConnectionPool) connectionPool.getValue();
        List<Connection> connections = value1.getConnections();
        for (int i = 0; i < connections.size(); i++) {
          AbstractConnection connection = (AbstractConnection) connections.get(i);
          Channel channel = connection.getChannel();
          builder.append(" isActive: ").append(channel.isActive());
          builder.append(" isRegistered: ").append(channel.isRegistered());
          builder.append(" isOpen: ").append(channel.isOpen());
          builder.append(" isWritable: ").append(channel.isWritable());
        }
      }
    }
    return builder.toString();
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
