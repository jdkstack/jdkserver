package org.jdkstack.jdkserver.http.core;

import java.util.List;
import javax.net.ssl.SNIServerName;

/**
 * Encapsulates the security capabilities of an SSL/TLS connection.
 *
 * <p>The security capabilities are the list of ciphersuites to be accepted in an SSL/TLS handshake,
 * the record version, the hello version, and server name indication, etc., of an SSL/TLS
 * connection.
 *
 * <p><code>SSLCapabilities</code> can be retrieved by exploring the network data of an SSL/TLS
 * connection via {@link SSLExplorer#explore(ByteBuffer)} or {@link SSLExplorer#explore(byte[], int,
 * int)}.
 *
 * @see SSLExplorer
 */
public abstract class SSLCapabilities {
  /**
   * Returns the record version of an SSL/TLS connection
   *
   * @return a non-null record version
   */
  public abstract String getRecordVersion();

  /**
   * Returns the hello version of an SSL/TLS connection
   *
   * @return a non-null hello version
   */
  public abstract String getHelloVersion();

  /**
   * Returns a <code>List</code> containing all {@link SNIServerName}s of the server name
   * indication.
   *
   * @return a non-null immutable list of {@link SNIServerName}s of the server name indication
   *     parameter, may be empty if no server name indication.
   * @see SNIServerName
   */
  public abstract List<SNIServerName> getServerNames();
}
