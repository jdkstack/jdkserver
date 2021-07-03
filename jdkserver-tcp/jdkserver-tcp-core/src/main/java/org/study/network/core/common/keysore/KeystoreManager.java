package org.study.network.core.common.keysore;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.study.network.core.common.option.KeystoreOptions;
import org.study.network.core.common.option.TrustKeystoreOptions;

/**
 * 实现SSL.
 *
 * <p>证书只支持PKCS12类型,支持PEM扩展,不支持JKS.
 *
 * @author admin
 * @version 2021-02-24 12:09
 * @since 2021-02-24 12:09:00
 */
public class KeystoreManager {
  /** 存储keystore每一个x509实体对象,包括key和证书. */
  public static final Map<String, X509KeyManager> X509_KEY_MANAGER = new HashMap<>(16);
  /** 存储keystore每一个x509实体对象,包括key和证书. */
  public static final Map<String, X509KeyManager> WILDCARD_X509_KEY_MANAGER = new HashMap<>(16);
  /** 存储trust keystore每一个CA证书. */
  public static final Map<String, TrustManagerFactory> TRUST_MANAGER_FACTORY = new HashMap<>(16);
  /** 存储完整的keystore. */
  public static final Map<String, KeyManagerFactory> KEY_MANAGER_FACTORY = new HashMap<>(16);
  /** 初始化所有的上下文对象. */
  public static final Map<String, SslContext> sslContextMap = new ConcurrentHashMap<>(16);
  /** 用来解析PEM文件的开始匹配规则. */
  private static final Pattern BEGIN_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  /** 用来解析PEM文件的结尾匹配规则. */
  private static final Pattern END_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");

  public static void main(String[] args) {
    try {
      // String keyStorePath = "F:\\ca\\server";
      // parserKeystore(keyStorePath, "xxxxxx", "PKCS12");

      // String trustStorePath = "F:\\ca\\server-t";
      // parserTrustKeystore(trustStorePath, "xxxxxx", "PKCS12");
      // initServerContext();
      KeyStore ks =
          pem2Keystore(
              "www.server.com",
              "C:\\OpenSSL\\SSL\\key.pem",
              "C:\\OpenSSL\\SSL\\cert.pem",
              "xxxxxx",
              "PKCS12");
      KeyStore kst = pem2TrustKeystore("www.server.com", "C:\\OpenSSL\\SSL\\cert.pem", "PKCS12");
      parserPemKeystore(ks, "xxxxxx");
      parserPemTrustKeystore(kst, "PKCS12");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 读取keystore文件,并初始化Keystore.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static KeyStore readFile(String path, String password, String type) throws Exception {
    Path target = new File(path).toPath();
    byte[] bytes = Files.readAllBytes(target);
    KeyStore ks = KeyStore.getInstance(type);
    try (InputStream in = new ByteArrayInputStream(bytes)) {
      ks.load(in, password.toCharArray());
    }
    return ks;
  }

  /**
   * 读取PEM文件的证书实体.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private static X509Certificate[] getCertificate(byte[] certificateValue) throws Exception {
    if (certificateValue == null) {
      throw new RuntimeException("Missing X.509 certificate path");
    }
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    String pem = new String(certificateValue, "UTF-8");
    List<X509Certificate> pems = new ArrayList<>();
    Matcher beginMatcher = BEGIN_PATTERN.matcher(pem);
    Matcher endMatcher = END_PATTERN.matcher(pem);
    while (true) {
      boolean begin = beginMatcher.find();
      if (!begin) {
        break;
      }
      String beginDelimiter = beginMatcher.group(1);
      boolean end = endMatcher.find();
      if (!end) {
        throw new RuntimeException("Missing -----END " + beginDelimiter + "----- delimiter");
      } else {
        String endDelimiter = endMatcher.group(1);
        if (!beginDelimiter.equals(endDelimiter)) {
          throw new RuntimeException("Missing -----END " + beginDelimiter + "----- delimiter");
        } else {
          String content = pem.substring(beginMatcher.end(), endMatcher.start());
          content = content.replaceAll("\\s", "");
          if (content.length() == 0) {
            throw new RuntimeException("Empty pem file");
          }
          byte[] decode = Base64.getDecoder().decode(content);
          Collection<X509Certificate> pemItems = null;
          if ("CERTIFICATE".equals(endDelimiter)) {
            pemItems =
                (Collection<X509Certificate>)
                    certFactory.generateCertificates(new ByteArrayInputStream(decode));
          } else {
            pemItems = Collections.emptyList();
          }
          pems.addAll(pemItems);
        }
      }
    }
    if (pems.isEmpty()) {
      throw new RuntimeException("Missing -----BEGIN CERTIFICATE----- delimiter");
    }
    return pems.toArray(new X509Certificate[0]);
  }

  /**
   * 读取PEM文件的key实体.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private static PrivateKey getPrivateKey(byte[] keyValue) throws Exception {
    if (keyValue == null) {
      throw new RuntimeException("Missing private key path");
    }
    KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
    KeyFactory ecKeyFactory = KeyFactory.getInstance("EC");

    String pem = new String(keyValue, "UTF-8");
    List<PrivateKey> pems = new ArrayList<>();
    Matcher beginMatcher = BEGIN_PATTERN.matcher(pem);
    Matcher endMatcher = END_PATTERN.matcher(pem);
    while (true) {
      boolean begin = beginMatcher.find();
      if (!begin) {
        break;
      }
      String beginDelimiter = beginMatcher.group(1);
      boolean end = endMatcher.find();
      if (!end) {
        throw new RuntimeException("Missing -----END " + beginDelimiter + "----- delimiter");
      } else {
        String endDelimiter = endMatcher.group(1);
        if (!beginDelimiter.equals(endDelimiter)) {
          throw new RuntimeException("Missing -----END " + beginDelimiter + "----- delimiter");
        } else {
          String content = pem.substring(beginMatcher.end(), endMatcher.start());
          content = content.replaceAll("\\s", "");
          if (content.length() == 0) {
            throw new RuntimeException("Empty pem file");
          }
          byte[] decode = Base64.getDecoder().decode(content);
          Collection<PrivateKey> pemItems = null;
          if ("RSA PRIVATE KEY".equals(endDelimiter)) {
            pemItems =
                Collections.singletonList(
                    rsaKeyFactory.generatePrivate(PrivateKeyParser.getRSAKeySpec(decode)));
          }
          if ("PRIVATE KEY".equals(endDelimiter)) {
            String algorithm = PrivateKeyParser.getPKCS8EncodedKeyAlgorithm(decode);
            if (rsaKeyFactory.getAlgorithm().equals(algorithm)) {
              pemItems =
                  Collections.singletonList(
                      rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(decode)));
            } else if (ecKeyFactory != null && ecKeyFactory.getAlgorithm().equals(algorithm)) {
              pemItems =
                  Collections.singletonList(
                      ecKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(decode)));
            }
          }
          pems.addAll(pemItems);
        }
      }
    }
    return pems.get(0);
  }

  /**
   * 将PEM key 和cert文件转化成Keystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static KeyStore pem2Keystore(
      String alias, String key, String cert, String password, String type) throws Exception {
    Path keyPath = new File(key).toPath();
    byte[] keyBytes = Files.readAllBytes(keyPath);
    Path certPath = new File(cert).toPath();
    byte[] certBytes = Files.readAllBytes(certPath);
    final KeyStore keyStoreKey = KeyStore.getInstance(type);
    keyStoreKey.load(null, null);
    PrivateKey privateKey = getPrivateKey(keyBytes);
    Certificate[] certificateChain = getCertificate(certBytes);
    keyStoreKey.setEntry(
        alias,
        new KeyStore.PrivateKeyEntry(privateKey, certificateChain),
        new KeyStore.PasswordProtection(password.toCharArray()));
    return keyStoreKey;
  }

  /**
   * 将PEM CA cert文件转化成TrustKeystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static KeyStore pem2TrustKeystore(String alias, String cert, String type)
      throws Exception {
    Path certPath = new File(cert).toPath();
    byte[] certBytes = Files.readAllBytes(certPath);
    final KeyStore keyStoreKey = KeyStore.getInstance(type);
    keyStoreKey.load(null, null);
    Certificate[] certificateChain = getCertificate(certBytes);
    keyStoreKey.setCertificateEntry(alias, certificateChain[0]);
    return keyStoreKey;
  }

  /**
   * 解析PemKeystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserPemKeystore(KeyStore ks, String password) throws Exception {
    // 所有证书的放在单个库里.
    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(ks, password.toCharArray());
    KEY_MANAGER_FACTORY.put("keyManagerFactory", keyManagerFactory);
    Enumeration<String> en = ks.aliases();
    while (en.hasMoreElements()) {
      String alias = en.nextElement();
      Certificate cert = ks.getCertificate(alias);
      if (!ks.isKeyEntry(alias) && !(cert instanceof X509Certificate)) {
        continue;
      }
      PrivateKey key = (PrivateKey) ks.getKey(alias, password.toCharArray());
      Certificate[] tmp = ks.getCertificateChain(alias);
      if (tmp == null) {
        // It's a private key
        continue;
      }
      List<X509Certificate> chain =
          Arrays.stream(tmp).map(c -> (X509Certificate) c).collect(Collectors.toList());
      X509Certificate x509Cert = (X509Certificate) cert;
      Collection<List<?>> ans = x509Cert.getSubjectAlternativeNames();
      List<String> domains = new ArrayList<>();
      if (ans != null) {
        for (List<?> l : ans) {
          if (l.size() == 2 && l.get(0) instanceof Number && ((Number) l.get(0)).intValue() == 2) {
            String dns = l.get(1).toString();
            domains.add(dns);
          }
        }
      }
      String dn = x509Cert.getSubjectX500Principal().getName();
      List<String> names = new ArrayList<>();
      LdapName ldapDN = new LdapName(dn);
      for (Rdn rdn : ldapDN.getRdns()) {
        if (rdn.getType().equalsIgnoreCase("cn")) {
          String name = rdn.getValue().toString();
          names.add(name);
        }
      }
      domains.addAll(names);
      for (String domain : domains) {
        StudyX509KeyManager studyX509KeyManager = new StudyX509KeyManager(key, chain, domain);
        if (domain.startsWith("*.")) {
          WILDCARD_X509_KEY_MANAGER.put(domain.substring(2), studyX509KeyManager);
        } else {
          X509_KEY_MANAGER.put(domain, studyX509KeyManager);
        }
      }
    }
  }

  /**
   * 解析PemTrustKeystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserPemTrustKeystore(KeyStore ks, String type) throws Exception {
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(ks);
    TRUST_MANAGER_FACTORY.put("trustManagerFactory", trustManagerFactory);
    Enumeration<String> en = ks.aliases();
    while (en.hasMoreElements()) {
      String alias = en.nextElement();
      Certificate cert = ks.getCertificate(alias);
      if (ks.isCertificateEntry(alias) && !alias.startsWith("cert-")) {
        final KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("cert-1", cert);
        TrustManagerFactory fact =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        fact.init(keyStore);
        TRUST_MANAGER_FACTORY.put(alias, fact);
      }
    }
  }

  /**
   * 直接解析Keystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserKeystore(KeystoreOptions keystoreOptions) throws Exception {
    String path = keystoreOptions.getKeyStorePath();
    URL resource = KeystoreManager.class.getResource(path);
    String file = resource.getFile();
    String password = keystoreOptions.getKeyStorePassword();
    String type = keystoreOptions.getKeyStoreType();
    KeyStore ks = readFile(file, password, type);
    // 所有证书的放在单个库里.
    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(ks, password.toCharArray());
    KEY_MANAGER_FACTORY.put("keyManagerFactory", keyManagerFactory);
    Enumeration<String> en = ks.aliases();
    while (en.hasMoreElements()) {
      String alias = en.nextElement();
      Certificate cert = ks.getCertificate(alias);
      if (!ks.isKeyEntry(alias) && !(cert instanceof X509Certificate)) {
        continue;
      }
      PrivateKey key = (PrivateKey) ks.getKey(alias, password.toCharArray());
      Certificate[] tmp = ks.getCertificateChain(alias);
      if (tmp == null) {
        // It's a private key
        continue;
      }
      List<X509Certificate> chain =
          Arrays.stream(tmp).map(c -> (X509Certificate) c).collect(Collectors.toList());
      X509Certificate x509Cert = (X509Certificate) cert;
      Collection<List<?>> ans = x509Cert.getSubjectAlternativeNames();
      List<String> domains = new ArrayList<>();
      if (ans != null) {
        for (List<?> l : ans) {
          if (l.size() == 2 && l.get(0) instanceof Number && ((Number) l.get(0)).intValue() == 2) {
            String dns = l.get(1).toString();
            domains.add(dns);
          }
        }
      }
      String dn = x509Cert.getSubjectX500Principal().getName();
      List<String> names = new ArrayList<>();
      LdapName ldapDN = new LdapName(dn);
      for (Rdn rdn : ldapDN.getRdns()) {
        if (rdn.getType().equalsIgnoreCase("cn")) {
          String name = rdn.getValue().toString();
          names.add(name);
        }
      }
      domains.addAll(names);
      for (String domain : domains) {
        StudyX509KeyManager studyX509KeyManager = new StudyX509KeyManager(key, chain, domain);
        if (domain.startsWith("*.")) {
          WILDCARD_X509_KEY_MANAGER.put(domain.substring(2), studyX509KeyManager);
        } else {
          X509_KEY_MANAGER.put(domain, studyX509KeyManager);
        }
      }
    }
  }

  /**
   * 直接解析TrustKeystore文件.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserTrustKeystore(TrustKeystoreOptions trustKeystoreOptions)
      throws Exception {
    String path = trustKeystoreOptions.getTrustKeyStorePath();
    URL resource = KeystoreManager.class.getResource(path);
    String file = resource.getFile();
    String password = trustKeystoreOptions.getTrustKeyStorePassword();
    String type = trustKeystoreOptions.getTrustKeyStoreType();
    KeyStore ks = readFile(file, password, type);
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(ks);
    TRUST_MANAGER_FACTORY.put("trustManagerFactory", trustManagerFactory);
    Enumeration<String> en = ks.aliases();
    while (en.hasMoreElements()) {
      String alias = en.nextElement();
      Certificate cert = ks.getCertificate(alias);
      if (ks.isCertificateEntry(alias) && !alias.startsWith("cert-")) {
        final KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("cert-1", cert);
        TrustManagerFactory fact =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        fact.init(keyStore);
        TRUST_MANAGER_FACTORY.put(alias, fact);
      }
    }
  }

  /**
   * 创建服务器端SslContextBuilder,指定单个X509key管理器.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private static SslContextBuilder serverContextBuilder(
      final PrivateKey key, final X509Certificate[] chain, final String password) {
    return SslContextBuilder.forServer(key, password, chain);
  }

  /**
   * 创建服务器端SslContextBuilder,指定整个key管理器.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private static SslContextBuilder serverContextBuilder(KeyManagerFactory keyMgrFactory) {
    return SslContextBuilder.forServer(keyMgrFactory);
  }

  /**
   * 初始化服务端上下文对象.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void initServerContext() throws Exception {
    for (Entry<String, X509KeyManager> entry : X509_KEY_MANAGER.entrySet()) {
      String key = entry.getKey();
      X509KeyManager x509KeyManager = entry.getValue();
      SslContextBuilder builder =
          serverContextBuilder(
              x509KeyManager.getPrivateKey(null), x509KeyManager.getCertificateChain(null), null);
      fill(builder);
      TrustManagerFactory fact = TRUST_MANAGER_FACTORY.get(key);
      builder.trustManager(fact);
      sslContextMap.put(key, builder.build());
    }
    for (Entry<String, X509KeyManager> entry : WILDCARD_X509_KEY_MANAGER.entrySet()) {
      String key = entry.getKey();
      X509KeyManager x509KeyManager = entry.getValue();
      PrivateKey privateKey = x509KeyManager.getPrivateKey(null);
      X509Certificate[] certificateChain = x509KeyManager.getCertificateChain(null);
      SslContextBuilder builder = serverContextBuilder(privateKey, certificateChain, null);
      fill(builder);
      TrustManagerFactory fact = TRUST_MANAGER_FACTORY.get(key);
      builder.trustManager(fact);
      sslContextMap.put(key, builder.build());
    }
    //
    KeyManagerFactory keyManagerFactory = KEY_MANAGER_FACTORY.get("keyManagerFactory");
    TrustManagerFactory trustManagerFactory = TRUST_MANAGER_FACTORY.get("trustManagerFactory");
    SslContextBuilder sslContextBuilder = serverContextBuilder(keyManagerFactory);
    fill(sslContextBuilder);
    sslContextBuilder.trustManager(trustManagerFactory);
    sslContextMap.put("serverManagerFactory", sslContextBuilder.build());
  }

  /**
   * 初始化客户端上下文对象.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void initClientContext(String clientName) throws Exception {
    SslContextBuilder builder = SslContextBuilder.forClient();
    if (clientName == null) {
      KeyManagerFactory keyManagerFactory = KEY_MANAGER_FACTORY.get("keyManagerFactory");
      TrustManagerFactory trustManagerFactory = TRUST_MANAGER_FACTORY.get("trustManagerFactory");
      builder.keyManager(keyManagerFactory);
      fill(builder);
      builder.trustManager(trustManagerFactory);
      sslContextMap.put("clientManagerFactory", builder.build());
    } else {
      X509KeyManager mgr = X509_KEY_MANAGER.get(clientName);
      if (mgr == null) {
        mgr = WILDCARD_X509_KEY_MANAGER.get(clientName);
      }
      TrustManagerFactory fact = TRUST_MANAGER_FACTORY.get(clientName);
      fill(builder);
      builder.trustManager(fact);
      if (mgr != null) {
        builder.keyManager(mgr);
      }
      sslContextMap.put(clientName, builder.build());
    }
  }

  /**
   * 填充builder 工具类.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void fill(SslContextBuilder builder) throws Exception {
    ArrayList<String> suite = new ArrayList<>();
    if (OpenSsl.isAvailable()) {
      builder.sslProvider(SslProvider.OPENSSL);
      suite.addAll(OpenSsl.availableOpenSslCipherSuites());
    } else {
      builder.sslProvider(SslProvider.JDK);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, null, null);
      SSLEngine engineCs = context.createSSLEngine();
      Collections.addAll(suite, engineCs.getEnabledCipherSuites());
    }
    builder.ciphers(suite);
  }

  /**
   * 服务器端SNI.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static SniHandler createServerSniHandler(final String sni) {
    return new SniHandler(
        serverName -> {
          SslContext sslContext = sslContextMap.get(serverName);
          SNIMatcher matcher = SNIHostName.createSNIMatcher(sni);
          Collection<SNIMatcher> matchers = new ArrayList<>(1);
          matchers.add(matcher);
          return new StudyDelegatingSslContext(sslContext, matchers);
        });
  }

  /**
   * 客户端SNI.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static SslHandler createClientSniHandler(
      String host, int port, boolean domainSocket, String clientName, String serverName)
      throws Exception {
    SslContext sslContext = sslContextMap.get(clientName);
    SSLEngine engine;
    if (domainSocket) {
      engine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
    } else {
      engine = sslContext.newEngine(ByteBufAllocator.DEFAULT, host, port);
    }
    engine.setUseClientMode(true);
    String[] protocols = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
    engine.setEnabledProtocols(protocols);
    SSLParameters sslParameters = engine.getSSLParameters();
    sslParameters.setServerNames(Collections.singletonList(new SNIHostName(serverName)));
    engine.setSSLParameters(sslParameters);
    return new SslHandler(engine);
  }

  /**
   * 服务器端SSL.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static SslHandler createServerSslHandler() {
    SslContext sslContext = sslContextMap.get("serverManagerFactory");
    SSLEngine engine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
    engine.setUseClientMode(false);
    String[] supportedProtocols = engine.getSupportedProtocols();
    engine.setEnabledProtocols(supportedProtocols);
    engine.setNeedClientAuth(true);
    return new SslHandler(engine);
  }

  /**
   * 客户端SSL.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static SslHandler createClientSslHandler() {
    SslContext sslContext = sslContextMap.get("clientManagerFactory");
    SSLEngine engine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
    engine.setUseClientMode(true);
    String[] supportedProtocols = engine.getSupportedProtocols();
    engine.setEnabledProtocols(supportedProtocols);
    return new SslHandler(engine);
  }
}
