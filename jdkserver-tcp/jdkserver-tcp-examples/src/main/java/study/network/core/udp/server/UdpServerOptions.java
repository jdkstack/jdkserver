package study.network.core.udp.server;

import study.network.core.common.option.NetworkOptions;
import study.network.core.tool.Arguments;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-01-20 14:58
 * @since 2021-01-20 14:58:00
 */
public class UdpServerOptions extends NetworkOptions {

  /** The default value of broadcast for the socket = false */
  public static final boolean DEFAULT_BROADCAST = false;

  /** The default value of loopback disabled = true */
  public static final boolean DEFAULT_LOOPBACK_MODE_DISABLED = true;

  /** The default value of multicast disabled = -1 */
  public static final int DEFAULT_MULTICAST_TIME_TO_LIVE = -1;

  /** The default value of multicast network interface = null */
  public static final String DEFAULT_MULTICAST_NETWORK_INTERFACE = null;

  /** The default value of reuse address = false */
  public static final boolean DEFAULT_REUSE_ADDRESS = false; // Override this

  /** The default value of use IP v6 = false */
  public static final boolean DEFAULT_IPV6 = false;

  private boolean broadcast;
  private boolean loopbackModeDisabled;
  private int multicastTimeToLive;
  private String multicastNetworkInterface;
  private boolean ipV6;

  /** Default constructor */
  public UdpServerOptions() {
    super();
    init();
    setReuseAddress(DEFAULT_REUSE_ADDRESS); // default is different for DatagramSocket
  }

  private void init() {
    broadcast = DEFAULT_BROADCAST;
    loopbackModeDisabled = DEFAULT_LOOPBACK_MODE_DISABLED;
    multicastTimeToLive = DEFAULT_MULTICAST_TIME_TO_LIVE;
    multicastNetworkInterface = DEFAULT_MULTICAST_NETWORK_INTERFACE;
    ipV6 = DEFAULT_IPV6;
  }

  @Override
  public int getSendBufferSize() {
    return super.getSendBufferSize();
  }

  @Override
  public UdpServerOptions setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }

  @Override
  public int getReceiveBufferSize() {
    return super.getReceiveBufferSize();
  }

  @Override
  public UdpServerOptions setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }

  @Override
  public UdpServerOptions setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }

  @Override
  public UdpServerOptions setReusePort(boolean reusePort) {
    return (UdpServerOptions) super.setReusePort(reusePort);
  }

  @Override
  public int getTrafficClass() {
    return super.getTrafficClass();
  }

  @Override
  public UdpServerOptions setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }

  /** @return true if the socket can send or receive broadcast packets? */
  public boolean isBroadcast() {
    return broadcast;
  }

  /**
   * Set if the socket can send or receive broadcast packets
   *
   * @param broadcast true if the socket can send or receive broadcast packets
   * @return a reference to this, so the API can be used fluently
   */
  public UdpServerOptions setBroadcast(boolean broadcast) {
    this.broadcast = broadcast;
    return this;
  }

  /** @return true if loopback mode is disabled */
  public boolean isLoopbackModeDisabled() {
    return loopbackModeDisabled;
  }

  /**
   * Set if loopback mode is disabled
   *
   * @param loopbackModeDisabled true if loopback mode is disabled
   * @return a reference to this, so the API can be used fluently
   */
  public UdpServerOptions setLoopbackModeDisabled(boolean loopbackModeDisabled) {
    this.loopbackModeDisabled = loopbackModeDisabled;
    return this;
  }

  /** @return the multicast ttl value */
  public int getMulticastTimeToLive() {
    return multicastTimeToLive;
  }

  /**
   * Set the multicast ttl value
   *
   * @param multicastTimeToLive the multicast ttl value
   * @return a reference to this, so the API can be used fluently
   */
  public UdpServerOptions setMulticastTimeToLive(int multicastTimeToLive) {
    Arguments.require(multicastTimeToLive >= 0, "multicastTimeToLive must be >= 0");
    this.multicastTimeToLive = multicastTimeToLive;
    return this;
  }

  /**
   * Get the multicast network interface address
   *
   * @return the interface address
   */
  public String getMulticastNetworkInterface() {
    return multicastNetworkInterface;
  }

  /**
   * Set the multicast network interface address
   *
   * @param multicastNetworkInterface the address
   * @return a reference to this, so the API can be used fluently
   */
  public UdpServerOptions setMulticastNetworkInterface(String multicastNetworkInterface) {
    this.multicastNetworkInterface = multicastNetworkInterface;
    return this;
  }

  /** @return true if IP v6 be used? */
  public boolean isIpV6() {
    return ipV6;
  }

  /**
   * Set if IP v6 should be used
   *
   * @param ipV6 true if IP v6 should be used
   * @return a reference to this, so the API can be used fluently
   */
  public UdpServerOptions setIpV6(boolean ipV6) {
    this.ipV6 = ipV6;
    return this;
  }

  @Override
  public UdpServerOptions setLogActivity(boolean logEnabled) {
    return (UdpServerOptions) super.setLogActivity(logEnabled);
  }
}
