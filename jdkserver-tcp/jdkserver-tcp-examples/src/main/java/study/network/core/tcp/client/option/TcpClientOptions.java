package study.network.core.tcp.client.option;

import io.netty.channel.Channel;
import study.core.future.Handler;
import study.network.core.common.option.NetworkOptions;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-20 13:50
 * @since 2021-01-20 13:50:00
 */
public class TcpClientOptions extends NetworkOptions {

  /** The default value of connect timeout = 60000 ms */
  public static final int DEFAULT_CONNECT_TIMEOUT = 5000;

  /** The default value of whether all servers (SSL/TLS) should be trusted = false */
  public static final boolean DEFAULT_TRUST_ALL = false;

  /** The default value of the client metrics = "": */
  public static final String DEFAULT_METRICS_NAME = "";

  private int localPort;
  private String localHost;
  private int remotePort;
  private String remoteHost;
  private int connectTimeout;
  private boolean trustAll;
  private String metricsName;
  private String localAddress = "127.0.0.1";
  private boolean domainSocket;
  private Handler<Channel> worker;

  public TcpClientOptions() {
    init();
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

  public int getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public String getRemoteHost() {
    return remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  private void init() {
    this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    this.trustAll = DEFAULT_TRUST_ALL;
    this.metricsName = DEFAULT_METRICS_NAME;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public boolean isTrustAll() {
    return trustAll;
  }

  public void setTrustAll(boolean trustAll) {
    this.trustAll = trustAll;
  }

  public String getMetricsName() {
    return metricsName;
  }

  public void setMetricsName(String metricsName) {
    this.metricsName = metricsName;
  }

  public String getLocalAddress() {
    return localAddress;
  }

  public void setLocalAddress(String localAddress) {
    this.localAddress = localAddress;
  }

  public boolean isDomainSocket() {
    return domainSocket;
  }

  public void setDomainSocket(boolean domainSocket) {
    this.domainSocket = domainSocket;
  }
}
