package study.network.core.tcp.client.example;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.DelegatingSslContext;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.internal.PlatformDependent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import study.network.core.common.exception.StudyException;
import study.network.core.common.keysore.PrivateKeyParser;

/**
 * This is a class description.
 *
 * <p>Another description after blank line.
 *
 * @author admin
 * @version 2021-02-25 14:26
 * @since 2021-02-25 14:26:00
 */
public class Cert {
  private static final String DUMMY_PASSWORD = "xxxxxx";
  private static final String DUMMY_CERT_ALIAS = "cert-";
  private static final Pattern BEGIN_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  private static final Pattern END_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");
  private static final Map<String, X509KeyManager> wildcardMgrMap = new HashMap<>();
  private static final Map<String, X509KeyManager> mgrMap = new HashMap<>();
  private static final Map<String, TrustManagerFactory> trustMgrMap = new HashMap<>();
  private static final Map<Certificate, SslContext> sslContextMap = new ConcurrentHashMap<>();
  private static final String type = "PKCS12";

  public static SslHandler getSsl() throws Exception {
    List<String> crlPaths = new ArrayList<>(16);
    List<String> crlValues = new ArrayList<>(16);
    Path target =
        new File(
                "F:\\study\\study-backend-manage\\study-backend-manage-jaas\\target\\classes\\serverP12")
            .toPath();
    byte[] bytes = Files.readAllBytes(target);
    KeyStore ks = KeyStore.getInstance(type);
    try (InputStream in = new ByteArrayInputStream(bytes)) {
      ks.load(in, DUMMY_PASSWORD.toCharArray());
    }
    extracted(ks);
    KeyManagerFactory keyFact =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyFact.init(ks, DUMMY_PASSWORD.toCharArray());

    KeyStore ksKey = KeyStore.getInstance(type);
    Path target_t =
        new File(
                "F:\\study\\study-backend-manage\\study-backend-manage-jaas\\target\\classes\\serverP12-t")
            .toPath();
    byte[] bytesKey = Files.readAllBytes(target_t);
    try (InputStream inKey = new ByteArrayInputStream(bytesKey)) {
      ksKey.load(inKey, DUMMY_PASSWORD.toCharArray());
    }
    extracted(ksKey);

    TrustManagerFactory trustMgrFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustMgrFactory.init(ksKey);

    Collection<String> suite = new ArrayList<>(16);
    SslContextBuilder builder = SslContextBuilder.forServer(keyFact);
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
    builder.trustManager(trustMgrFactory);
    builder.ciphers(suite);
    SslContext sslContext = builder.build();
    SSLEngine engine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
    engine.setUseClientMode(false);
    String[] supportedProtocols = engine.getSupportedProtocols();
    engine.setEnabledProtocols(supportedProtocols);
    engine.setNeedClientAuth(true);
    return new SslHandler(engine);
  }

  private static void extracted(KeyStore ks) throws Exception {
    Enumeration<String> en = ks.aliases();
    while (en.hasMoreElements()) {
      String alias = en.nextElement();
      Certificate cert = ks.getCertificate(alias);
      if (ks.isCertificateEntry(alias) && !alias.startsWith(DUMMY_CERT_ALIAS)) {
        final KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("cert-1", cert);
        TrustManagerFactory fact =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        fact.init(keyStore);
        trustMgrMap.put(alias, fact);
      }
      if (ks.isKeyEntry(alias) && cert instanceof X509Certificate) {
        X509Certificate x509Cert = (X509Certificate) cert;
        Collection<List<?>> ans = x509Cert.getSubjectAlternativeNames();
        List<String> domains = new ArrayList<>();
        if (ans != null) {
          for (List<?> l : ans) {
            if (l.size() == 2
                && l.get(0) instanceof Number
                && ((Number) l.get(0)).intValue() == 2) {
              String dns = l.get(1).toString();
              domains.add(dns);
            }
          }
        }
        String dn = x509Cert.getSubjectX500Principal().getName();
        List<String> names = new ArrayList<>();
        if (!PlatformDependent.isAndroid()) {
          LdapName ldapDN = new LdapName(dn);
          for (Rdn rdn : ldapDN.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("cn")) {
              String name = rdn.getValue().toString();
              names.add(name);
            }
          }
        } else {
          String[] rdns = dn.trim().split("[,;]");
          for (String rdn : rdns) {
            String[] nvp = rdn.trim().split("=");
            if (nvp.length == 2 && "cn".equalsIgnoreCase(nvp[0])) {
              names.add(nvp[1]);
            }
          }
        }
        domains.addAll(names);
        if (!domains.isEmpty()) {
          PrivateKey key = (PrivateKey) ks.getKey(alias, DUMMY_PASSWORD.toCharArray());
          Certificate[] tmp = ks.getCertificateChain(alias);
          if (tmp == null) {
            // It's a private key
            continue;
          }
          List<X509Certificate> chain =
              Arrays.asList(tmp).stream()
                  .map(c -> (X509Certificate) c)
                  .collect(Collectors.toList());
          X509KeyManager mgr =
              new X509KeyManager() {
                @Override
                public String[] getClientAliases(String s, Principal[] principals) {
                  throw new UnsupportedOperationException();
                }

                @Override
                public String chooseClientAlias(
                    String[] strings, Principal[] principals, Socket socket) {
                  throw new UnsupportedOperationException();
                }

                @Override
                public String[] getServerAliases(String s, Principal[] principals) {
                  throw new UnsupportedOperationException();
                }

                @Override
                public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
                  throw new UnsupportedOperationException();
                }

                @Override
                public X509Certificate[] getCertificateChain(String s) {
                  return chain.toArray(new X509Certificate[0]);
                }

                @Override
                public PrivateKey getPrivateKey(String s) {
                  return key;
                }
              };
          for (String domain : domains) {
            if (domain.startsWith("*.")) {
              wildcardMgrMap.put(domain.substring(2), mgr);
            } else {
              mgrMap.put(domain, mgr);
            }
          }
        }
      }
    }
  }

  private static <P> List<P> loadPems(
      byte[] data, BiFunction<String, byte[], Collection<P>> pemFact) throws IOException {
    String pem = new String(data, "UTF-8");
    List<P> pems = new ArrayList<>();
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
          Collection<P> pemItems = pemFact.apply(endDelimiter, Base64.getDecoder().decode(content));
          pems.addAll(pemItems);
        }
      }
    }
    return pems;
  }

  private static PrivateKey loadPrivateKey(byte[] keyValue) throws Exception {
    if (keyValue == null) {
      throw new RuntimeException("Missing private key path");
    }
    KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
    KeyFactory ecKeyFactory = KeyFactory.getInstance("EC");
    List<PrivateKey> pems =
        loadPems(
            keyValue,
            (delimiter, content) -> {
              try {
                switch (delimiter) {
                  case "RSA PRIVATE KEY":
                    return Collections.singletonList(
                        rsaKeyFactory.generatePrivate(PrivateKeyParser.getRSAKeySpec(content)));
                  case "PRIVATE KEY":
                    // in PKCS#8 the key algorithm is indicated at the beginning of the ASN.1
                    // structure
                    // so we can use the corresponding key factory once we know the algorithm name
                    String algorithm = PrivateKeyParser.getPKCS8EncodedKeyAlgorithm(content);
                    if (rsaKeyFactory.getAlgorithm().equals(algorithm)) {
                      return Collections.singletonList(
                          rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(content)));
                    } else if (ecKeyFactory != null
                        && ecKeyFactory.getAlgorithm().equals(algorithm)) {
                      return Collections.singletonList(
                          ecKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(content)));
                    }
                  default:
                    return Collections.emptyList();
                }
              } catch (InvalidKeySpecException e) {
                throw new StudyException(e);
              }
            });
    if (pems.isEmpty()) {
      throw new RuntimeException(
          "Missing -----BEGIN PRIVATE KEY----- or -----BEGIN RSA PRIVATE KEY----- delimiter");
    }
    return pems.get(0);
  }

  private static X509Certificate[] loadCerts(byte[] buffer) throws Exception {
    if (buffer == null) {
      throw new RuntimeException("Missing X.509 certificate path");
    }
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    List<X509Certificate> certs =
        loadPems(
            buffer,
            (delimiter, content) -> {
              try {
                switch (delimiter) {
                  case "CERTIFICATE":
                    return (Collection<X509Certificate>)
                        certFactory.generateCertificates(new ByteArrayInputStream(content));
                  default:
                    return Collections.emptyList();
                }
              } catch (CertificateException e) {
                throw new StudyException(e);
              }
            });
    if (certs.isEmpty()) {
      throw new RuntimeException("Missing -----BEGIN CERTIFICATE----- delimiter");
    }
    return certs.toArray(new X509Certificate[0]);
  }

  public static SslHandler pem() throws Exception {
    List<String> certPaths = new ArrayList<>(16);
    List<byte[]> certValues = new ArrayList<>(16);
    List<String> keyPaths = new ArrayList<>(16);
    List<byte[]> keyValues = new ArrayList<>(16);
    keyPaths.add("C:\\OpenSSL\\SSL\\key.pem");
    certPaths.add("C:\\OpenSSL\\SSL\\cert.pem");
    for (String keyPath : keyPaths) {
      Path target_t = new File(keyPath).toPath();
      byte[] bytesKey = Files.readAllBytes(target_t);
      keyValues.add(bytesKey);
    }

    for (String certPath : certPaths) {
      Path target_t = new File(certPath).toPath();
      byte[] bytesKey = Files.readAllBytes(target_t);
      certValues.add(bytesKey);
    }

    final KeyStore keyStoreKey = KeyStore.getInstance(type);
    keyStoreKey.load(null, null);

    for (int i = 0; i < keyValues.size(); i++) {
      PrivateKey key = loadPrivateKey(keyValues.get(i));
      Certificate[] chain = loadCerts(certValues.get(i));
      keyStoreKey.setEntry(
          "dummy-entry-" + i++,
          new KeyStore.PrivateKeyEntry(key, chain),
          new KeyStore.PasswordProtection(DUMMY_PASSWORD.toCharArray()));
    }
    extracted(keyStoreKey);
    KeyManagerFactory factKey =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    factKey.init(keyStoreKey, DUMMY_PASSWORD.toCharArray());

    List<String> certPathsT = new ArrayList<>(16);
    List<byte[]> certValuesT = new ArrayList<>(16);
    certPathsT.add("C:\\OpenSSL\\SSL\\cert.pem");
    for (String certPath : certPathsT) {
      Path target_t = new File(certPath).toPath();
      byte[] bytesKey = Files.readAllBytes(target_t);
      certValuesT.add(bytesKey);
    }
    final KeyStore keyStoreTr = KeyStore.getInstance(type);
    keyStoreTr.load(null, null);
    for (int i = 0; i < certValuesT.size(); i++) {
      for (Certificate cert : loadCerts(certValuesT.get(i))) {
        keyStoreTr.setCertificateEntry(DUMMY_CERT_ALIAS + i++, cert);
      }
    }
    extracted(keyStoreTr);
    TrustManagerFactory fact =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    fact.init(keyStoreTr);
    Collection<String> suite = new ArrayList<>(16);
    SslContextBuilder builder = SslContextBuilder.forServer(factKey);
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
    builder.trustManager(fact);
    builder.ciphers(suite);
    SslContext sslContext = builder.build();
    SSLEngine engine = sslContext.newEngine(ByteBufAllocator.DEFAULT);
    engine.setUseClientMode(false);
    List<String> strings = new ArrayList<>();
    strings.addAll(Arrays.asList(engine.getSupportedProtocols()));
    if (strings.isEmpty()) {
      // LOG.info("no SSL/TLS protocols are enabled due to configuration restrictions.");
    }
    engine.setEnabledProtocols(strings.toArray(new String[strings.size()]));
    engine.setNeedClientAuth(true);
    return new SslHandler(engine);
  }

  public static SniHandler sni() throws Exception {
    List<String> crlPaths = new ArrayList<>(16);
    List<String> crlValues = new ArrayList<>(16);
    Path target = new File("F:\\ca\\server").toPath();
    byte[] bytes = Files.readAllBytes(target);
    KeyStore ks = KeyStore.getInstance(type);
    try (InputStream in = new ByteArrayInputStream(bytes)) {
      ks.load(in, DUMMY_PASSWORD.toCharArray());
    }
    extracted(ks);

    String serverName = "www.server.com";
    X509KeyManager mgr = mgrMap.get(serverName);
    if (mgr == null && !wildcardMgrMap.isEmpty()) {
      int index = serverName.indexOf('.') + 1;
      if (index > 0) {
        String s = serverName.substring(index);
        mgr = wildcardMgrMap.get(s);
      }
    }

    KeyStore ksKey = KeyStore.getInstance(type);
    Path target_t = new File("F:\\ca\\server-t").toPath();
    byte[] bytesKey = Files.readAllBytes(target_t);
    try (InputStream inKey = new ByteArrayInputStream(bytesKey)) {
      ksKey.load(inKey, DUMMY_PASSWORD.toCharArray());
    }
    extracted(ksKey);

    TrustManagerFactory fact = trustMgrMap.get(serverName);
    if (fact == null) {
      fact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      fact.init(ksKey);
    }
    KeyManagerFactory keyFact =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyFact.init(ks, DUMMY_PASSWORD.toCharArray());
    // KeyManager[] keyManagers = keyFact.getKeyManagers();
    // X509ExtendedKeyManager keyManager = (X509ExtendedKeyManager)keyManagers[0];

    for (KeyManager keyManager : keyFact.getKeyManagers()) {
      if (keyManager instanceof X509ExtendedKeyManager) {
        X509ExtendedKeyManager x509KeyManager = (X509ExtendedKeyManager) keyManager;
      }
    }

    SslContextBuilder builder = SslContextBuilder.forServer(keyFact);
    builder.trustManager(fact);
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
    return new SniHandler(
        input -> {
          SslContext sslContext = null;
          try {
            sslContext = builder.build();
          } catch (SSLException e) {
            e.printStackTrace();
          }
          if (sslContext != null) {
            sslContext =
                new DelegatingSslContext(sslContext) {
                  @Override
                  protected void initEngine(SSLEngine engine) {
                    engine.setUseClientMode(false);
                    // final String[] supportedProtocols = engine.getSupportedProtocols();
                    Set<String> protocols = new LinkedHashSet<>();
                    protocols.add("TLSv1");
                    protocols.add("TLSv1.1");
                    protocols.add("TLSv1.2");
                    protocols.add("TLSv1.3");
                    protocols.retainAll(Arrays.asList(engine.getSupportedProtocols()));
                    // engine.setEnabledProtocols(supportedProtocols);
                    engine.setNeedClientAuth(true);
                    SSLParameters sslParameters = engine.getSSLParameters();
                    SNIMatcher matcher = SNIHostName.createSNIMatcher("www.server.com");
                    Collection<SNIMatcher> matchers = new ArrayList<>(1);
                    matchers.add(matcher);
                    sslParameters.setSNIMatchers(matchers);
                    engine.setSSLParameters(sslParameters);
                  }
                };
          }
          return sslContext;
        });
  }

  public static void main(String[] args) {
    try {
      pem();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
