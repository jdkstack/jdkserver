package org.study.network.core.tcp.server.option;

import io.netty.channel.Channel;
import org.study.core.future.Handler;
import org.study.network.core.common.option.NetworkOptions;

public class TcpServerOptions extends NetworkOptions {
  /** 事件线程的数量是CPU核心数的2倍. */
  public static final int DEFAULT_EVENT_LOOP_POOL_SIZE =
      2 * Runtime.getRuntime().availableProcessors();
  /** The default value of TCP send buffer size */
  public static final int DEFAULT_SEND_BUFFER_SIZE = 1024;

  /** The default value of TCP receive buffer size */
  public static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024;

  /** The default value of traffic class */
  public static final int DEFAULT_TRAFFIC_CLASS = 64;

  /** The default value of reuse address */
  public static final boolean DEFAULT_REUSE_ADDRESS = true;

  /** The default value of reuse port */
  public static final boolean DEFAULT_REUSE_PORT = false;

  /** The default log enabled = false */
  public static final boolean DEFAULT_LOG_ENABLED = false;

  /** The default value of TCP-no-delay = true (Nagle disabled) */
  public static final boolean DEFAULT_TCP_NO_DELAY = true;

  /** The default value of TCP keep alive = false */
  public static final boolean DEFAULT_TCP_KEEP_ALIVE = false;

  /** The default value of SO_linger = -1 */
  public static final int DEFAULT_SO_LINGER = 1024;

  /** The default accept backlog = 1024 */
  public static final int DEFAULT_ACCEPT_BACKLOG = 1024;

  private int acceptBacklog;
  private int soLinger;
  private boolean tcpNoDelay;

  private int localPort;
  private String localHost;
  private boolean domainSocket;
  private int workerCount;
  private Handler<Channel> worker;
  private boolean tcpKeepAlive;

  public int getWorkerCount() {
    return workerCount;
  }

  public void setWorkerCount(int workerCount) {
    this.workerCount = workerCount;
  }

  public Handler<Channel> getWorker() {
    return worker;
  }

  public void setWorker(Handler<Channel> worker) {
    this.worker = worker;
  }

  public int getLocalPort() {
    return localPort;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  public String getLocalHost() {
    return localHost;
  }

  public void setLocalHost(String localHost) {
    this.localHost = localHost;
  }

  public boolean isDomainSocket() {
    return domainSocket;
  }

  public void setDomainSocket(boolean domainSocket) {
    this.domainSocket = domainSocket;
  }

  public int getAcceptBacklog() {
    return acceptBacklog;
  }

  public void setAcceptBacklog(int acceptBacklog) {
    this.acceptBacklog = acceptBacklog;
  }

  @Override
  public int getSoLinger() {
    return this.soLinger;
  }

  @Override
  public void setSoLinger(final int soLinger) {
    this.soLinger = soLinger;
  }

  @Override
  public boolean isTcpNoDelay() {
    return this.tcpNoDelay;
  }

  @Override
  public void setTcpNoDelay(final boolean tcpNoDelay) {
    this.tcpNoDelay = tcpNoDelay;
  }

  @Override
  public boolean isTcpKeepAlive() {
    return this.tcpKeepAlive;
  }

  @Override
  public void setTcpKeepAlive(final boolean tcpKeepAlive) {
    this.tcpKeepAlive = tcpKeepAlive;
  }
}
