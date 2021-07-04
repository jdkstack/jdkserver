package study.network.core.tcp.client.option;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-03-03 15:36
 * @since 2021-03-03 15:36:00
 */
public class RpcClientOptions {
  /** . */
  private int remotePort;
  /** . */
  private String remoteHost;
  /** . */
  private int localPort;
  /** . */
  private String localHost;
  /** . */
  private int count;
  /** . */
  private String clientAlias;
  /** . */
  private String serverAlias;

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

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getClientAlias() {
    return clientAlias;
  }

  public void setClientAlias(String clientAlias) {
    this.clientAlias = clientAlias;
  }

  public String getServerAlias() {
    return serverAlias;
  }

  public void setServerAlias(String serverAlias) {
    this.serverAlias = serverAlias;
  }
}
