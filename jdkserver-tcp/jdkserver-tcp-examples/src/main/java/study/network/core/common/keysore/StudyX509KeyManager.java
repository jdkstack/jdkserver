package study.network.core.common.keysore;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-24 12:27
 * @since 2021-02-24 12:27:00
 */
public class StudyX509KeyManager extends X509ExtendedKeyManager {
  private X509ExtendedKeyManager keyManager;

  private PrivateKey key;

  private List<X509Certificate> chain;

  private String hostname;

  public StudyX509KeyManager(PrivateKey key, List<X509Certificate> chain, String hostname) {
    this.key = key;
    this.chain = chain;
    this.hostname = hostname;
  }

  @Override
  public String[] getClientAliases(String keyType, Principal[] issuers) {
    return this.getClientAliases(keyType, issuers);
  }

  @Override
  public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
    return this.chooseClientAlias(keyType, issuers, socket);
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    return this.getServerAliases(keyType, issuers);
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    return this.chooseServerAlias(keyType, issuers, socket);
  }

  @Override
  public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
    return hostname;
  }

  @Override
  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    ExtendedSSLSession extendedSSLSession = (ExtendedSSLSession) engine.getHandshakeSession();
    SSLSession session = engine.getSession();

    return hostname;
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    return chain.toArray(new X509Certificate[0]);
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    return key;
  }
}
