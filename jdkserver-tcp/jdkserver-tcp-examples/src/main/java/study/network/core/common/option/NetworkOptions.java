package study.network.core.common.option;

import study.network.core.tool.Arguments;

public abstract class NetworkOptions implements Options {

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

  private int sendBufferSize = 1024;
  private int receiveBufferSize = 1024;
  private int trafficClass = 64;
  private boolean reuseAddress;
  private boolean logActivity;
  private boolean reusePort;
  private boolean tcpNoDelay;
  private boolean tcpKeepAlive;
  private int soLinger = 1024;
  /** Default constructor */
  protected NetworkOptions() {
    sendBufferSize = DEFAULT_SEND_BUFFER_SIZE;
    receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
    reuseAddress = DEFAULT_REUSE_ADDRESS;
    trafficClass = DEFAULT_TRAFFIC_CLASS;
    logActivity = DEFAULT_LOG_ENABLED;
    reusePort = DEFAULT_REUSE_PORT;
  }

  /**
   * Copy constructor
   *
   * @param other the options to copy
   */
  protected NetworkOptions(NetworkOptions other) {
    this.sendBufferSize = other.getSendBufferSize();
    this.receiveBufferSize = other.getReceiveBufferSize();
    this.reuseAddress = other.isReuseAddress();
    this.reusePort = other.isReusePort();
    this.trafficClass = other.getTrafficClass();
    this.logActivity = other.logActivity;
  }

  /**
   * Return the TCP send buffer size, in bytes.
   *
   * @return the send buffer size
   */
  public int getSendBufferSize() {
    return sendBufferSize;
  }

  /**
   * Set the TCP send buffer size
   *
   * @param sendBufferSize the buffers size, in bytes
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setSendBufferSize(int sendBufferSize) {
    Arguments.require(
        sendBufferSize > 0 || sendBufferSize == DEFAULT_SEND_BUFFER_SIZE,
        "sendBufferSize must be > 0");
    this.sendBufferSize = sendBufferSize;
    return this;
  }

  /**
   * Return the TCP receive buffer size, in bytes
   *
   * @return the receive buffer size
   */
  public int getReceiveBufferSize() {
    return receiveBufferSize;
  }

  /**
   * Set the TCP receive buffer size
   *
   * @param receiveBufferSize the buffers size, in bytes
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setReceiveBufferSize(int receiveBufferSize) {
    Arguments.require(
        receiveBufferSize > 0 || receiveBufferSize == DEFAULT_RECEIVE_BUFFER_SIZE,
        "receiveBufferSize must be > 0");
    this.receiveBufferSize = receiveBufferSize;
    return this;
  }

  /** @return the value of reuse address */
  public boolean isReuseAddress() {
    return reuseAddress;
  }

  /**
   * Set the value of reuse address
   *
   * @param reuseAddress the value of reuse address
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setReuseAddress(boolean reuseAddress) {
    this.reuseAddress = reuseAddress;
    return this;
  }

  /** @return the value of traffic class */
  public int getTrafficClass() {
    return trafficClass;
  }

  /**
   * Set the value of traffic class
   *
   * @param trafficClass the value of traffic class
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setTrafficClass(int trafficClass) {
    Arguments.requireInRange(
        trafficClass, DEFAULT_TRAFFIC_CLASS, 255, "trafficClass tc must be 0 <= tc <= 255");
    this.trafficClass = trafficClass;
    return this;
  }

  /** @return true when network activity logging is enabled */
  public boolean getLogActivity() {
    return logActivity;
  }

  /**
   * Set to true to enabled network activity logging: Netty's pipeline is configured for logging on
   * Netty's logger.
   *
   * @param logActivity true for logging the network activity
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setLogActivity(boolean logActivity) {
    this.logActivity = logActivity;
    return this;
  }

  /** @return the value of reuse address - only supported by native transports */
  public boolean isReusePort() {
    return reusePort;
  }

  /**
   * Set the value of reuse port.
   *
   * <p>This is only supported by native transports.
   *
   * @param reusePort the value of reuse port
   * @return a reference to this, so the API can be used fluently
   */
  public NetworkOptions setReusePort(boolean reusePort) {
    this.reusePort = reusePort;
    return this;
  }

  public boolean isTcpNoDelay() {
    return tcpNoDelay;
  }

  public void setTcpNoDelay(boolean tcpNoDelay) {
    this.tcpNoDelay = tcpNoDelay;
  }

  public boolean isTcpKeepAlive() {
    return tcpKeepAlive;
  }

  public void setTcpKeepAlive(boolean tcpKeepAlive) {
    this.tcpKeepAlive = tcpKeepAlive;
  }

  public int getSoLinger() {
    return soLinger;
  }

  public void setSoLinger(int soLinger) {
    this.soLinger = soLinger;
  }
}
