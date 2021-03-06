package org.jdkstack.jdkserver.http.core.ssl.cert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
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
import org.jdkstack.jdkserver.http.api.Context;
import org.jdkstack.jdkserver.http.api.Exchange;
import org.jdkstack.jdkserver.http.api.Header;
import org.jdkstack.jdkserver.http.api.HttpHandler;
import org.jdkstack.jdkserver.http.core.HttpsConfigurator;
import org.jdkstack.jdkserver.http.core.HttpsParameters;
import org.jdkstack.jdkserver.http.core.PrivateKeyParser;
import org.jdkstack.jdkserver.http.core.SslContextBuilder;
import org.jdkstack.jdkserver.http.core.StudyX509KeyManager;
import org.jdkstack.jdkserver.http.core.option.KeystoreOptions;
import org.jdkstack.jdkserver.http.core.option.TrustKeystoreOptions;
import org.jdkstack.jdkserver.http.core.service.HttpsServer;
import org.jdkstack.jdkserver.http.core.spi.HttpServerProvider;

/**
 * ??????SSL.
 *
 * <p>???????????????PKCS12??????,??????PEM??????,?????????JKS.
 *
 * @author admin
 * @version 2021-02-24 12:09
 * @since 2021-02-24 12:09:00
 */
public class CertManager {
  /** ??????keystore?????????x509????????????,??????key?????????. */
  public static final Map<String, X509KeyManager> X509_KEY_MANAGER = new HashMap<>(16);
  /** ??????keystore?????????x509????????????,??????key?????????. */
  public static final Map<String, X509KeyManager> WILDCARD_X509_KEY_MANAGER = new HashMap<>(16);
  /** ??????trust keystore?????????CA??????. */
  public static final Map<String, TrustManagerFactory> TRUST_MANAGER_FACTORY = new HashMap<>(16);
  /** ???????????????keystore. */
  public static final Map<String, KeyManagerFactory> KEY_MANAGER_FACTORY = new HashMap<>(16);
  /** ?????????????????????????????????. */
  public static final Map<String, SSLContext> sslContextMap = new ConcurrentHashMap<>(16);
  /** ????????????PEM???????????????????????????. */
  private static final Pattern BEGIN_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  /** ????????????PEM???????????????????????????. */
  private static final Pattern END_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");

  private Provider sslContextProvider;
  private X509Certificate[] trustCertCollection;
  private TrustManagerFactory trustManagerFactory;
  private X509Certificate[] keyCertChain;
  private PrivateKey key;
  private String keyPassword;
  private KeyManagerFactory keyManagerFactory;
  private Iterable<String> ciphers;
  private long sessionCacheSize;
  private long sessionTimeout;
  private String[] protocols;
  private boolean startTls;
  private boolean enableOcsp;
  private String keyStoreType = KeyStore.getDefaultType();

  /**
   * ??????keystore??????,????????????Keystore.
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
   * ??????PEM?????????????????????.
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
   * ??????PEM?????????key??????.
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
   * ???PEM key ???cert???????????????Keystore??????.
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
   * ???PEM CA cert???????????????TrustKeystore??????.
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
   * ??????PemKeystore??????.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserPemKeystore(KeyStore ks, String password) throws Exception {
    // ?????????????????????????????????.
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
   * ??????PemTrustKeystore??????.
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
   * ????????????Keystore??????.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserKeystore(KeystoreOptions keystoreOptions) throws Exception {
    String path = keystoreOptions.getKeyStorePath();
    URL resource = CertManager.class.getResource(path);
    String file = resource.getFile();
    String password = keystoreOptions.getKeyStorePassword();
    String type = keystoreOptions.getKeyStoreType();
    KeyStore ks = readFile(file, password, type);
    // ?????????????????????????????????.
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
   * ????????????TrustKeystore??????.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void parserTrustKeystore(TrustKeystoreOptions trustKeystoreOptions)
      throws Exception {
    String path = trustKeystoreOptions.getTrustKeyStorePath();
    URL resource = CertManager.class.getResource(path);
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
   * ??????????????????SslContextBuilder,????????????X509key?????????.
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
   * ??????????????????SslContextBuilder,????????????key?????????.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  private static SslContextBuilder serverContextBuilder(KeyManagerFactory keyMgrFactory) {
    return SslContextBuilder.forServer(keyMgrFactory);
  }

  /**
   * ?????????????????????????????????.
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
   * ?????????????????????????????????.
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
   * ??????builder ?????????.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static void fill(SslContextBuilder builder) throws Exception {
    ArrayList<String> suite = new ArrayList<>();
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, null, null);
    SSLEngine engineCs = context.createSSLEngine();
    Collections.addAll(suite, engineCs.getEnabledCipherSuites());
    // builder.ciphers(suite);
  }

  /**
   * ????????????SNI.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static HttpHandler createServerSniHandler(final String sni) {

    return new HttpHandler() {
      @Override
      public void handle(Exchange exchange) throws IOException {
        byte[] bytes = "{\"XXXXXXXXX\":9999}".getBytes(StandardCharsets.UTF_8);
        Header responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(bytes);
        responseBody.flush();
        responseBody.close();
      }
    };
  }

  /**
   * ?????????SNI.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static HttpHandler createClientSniHandler(
      String host, int port, boolean domainSocket, String clientName, String serverName)
      throws Exception {
    SSLContext sslContext = sslContextMap.get(clientName);
    SSLEngine engine = sslContext.createSSLEngine(host, port);
    engine.setUseClientMode(true);
    String[] protocols = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
    engine.setEnabledProtocols(protocols);
    SSLParameters sslParameters = engine.getSSLParameters();
    sslParameters.setServerNames(Collections.singletonList(new SNIHostName(serverName)));
    engine.setSSLParameters(sslParameters);
    return new HttpHandler() {
      @Override
      public void handle(Exchange exchange) throws IOException {}
    };
  }

  /**
   * ????????????SSL.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static HttpHandler createServerSslHandler() {
    return new HttpHandler() {
      @Override
      public void handle(Exchange exchange) throws IOException {
        Context httpContext = exchange.getHttpContext();
        byte[] bytes = "{\"XXXXXXXXX\":9999}".getBytes(StandardCharsets.UTF_8);
        Header responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(bytes);
        responseBody.flush();
        //https://github.com/eclipse/jetty.project/issues/236
        // javax.net.ssl.SSLException: closing inbound before receiving peer's close_no
        responseBody.close();
      }
    };
  }

  /**
   * ?????????SSL.
   *
   * <p>Another description after blank line.
   *
   * @author admin
   */
  public static HttpHandler createClientSslHandler() {
    SSLContext sslContext = sslContextMap.get("clientManagerFactory");
    SSLEngine engine = sslContext.createSSLEngine();
    engine.setUseClientMode(true);
    String[] supportedProtocols = engine.getSupportedProtocols();
    engine.setEnabledProtocols(supportedProtocols);
    return new HttpHandler() {
      @Override
      public void handle(Exchange exchange) throws IOException {}
    };
  }

  public static String chooseApplicationProtocol(
      SSLEngine serverSocket, List<String> clientProtocols, String protocol, String cipherSuite) {
    // For example, check the cipher suite and return an application protocol
    // value based on that.
    if (cipherSuite.equals("<--a_particular_ciphersuite-->")) {
      return "three";
    } else {
      return "";
    }
  }

  public static SSLContext sslContextMap(String sni) {
    return sslContextMap.get(sni);
  }
}
