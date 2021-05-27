package org.jdkstack.jdkserver.http.core;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManagerFactory;

public final class SslContextBuilder {

  public static SslContextBuilder forClient() {
    return new SslContextBuilder(false);
  }

  public static SslContextBuilder forServer(
      PrivateKey key, String keyPassword, X509Certificate... keyCertChain) {
    return new SslContextBuilder(true).keyManager(key, keyPassword, keyCertChain);
  }

  public static SslContextBuilder forServer(KeyManagerFactory keyManagerFactory) {
    return new SslContextBuilder(true).keyManager(keyManagerFactory);
  }

  private final boolean forServer;
  private Provider sslContextProvider;
  private X509Certificate[] trustCertCollection;
  private TrustManagerFactory trustManagerFactory;
  private X509Certificate[] keyCertChain;
  private PrivateKey key;
  private String keyPassword;
  private KeyManagerFactory keyManagerFactory;
  private Iterable<String> ciphers;
  private long sessionCacheSize;
  private long sessionTimeout = 60L;
  private String[] protocols;
  private boolean startTls;
  private boolean enableOcsp;
  private String keyStoreType = KeyStore.getDefaultType();

  private SslContextBuilder(boolean forServer) {
    this.forServer = forServer;
  }

  public SslContextBuilder keyStoreType(String keyStoreType) {
    this.keyStoreType = keyStoreType;
    return this;
  }

  public SslContextBuilder sslContextProvider(Provider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return this;
  }

  public SslContextBuilder trustManager(X509Certificate... trustCertCollection) {
    this.trustCertCollection = trustCertCollection != null ? trustCertCollection.clone() : null;
    trustManagerFactory = null;
    return this;
  }

  public SslContextBuilder trustManager(Iterable<? extends X509Certificate> trustCertCollection) {
    return trustManager(toArray(trustCertCollection, EmptyArrays.EMPTY_X509_CERTIFICATES));
  }

  public SslContextBuilder trustManager(TrustManagerFactory trustManagerFactory) {
    trustCertCollection = null;
    this.trustManagerFactory = trustManagerFactory;
    return this;
  }

  public SslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain) {
    return keyManager(key, null, keyCertChain);
  }

  public SslContextBuilder keyManager(
      PrivateKey key, Iterable<? extends X509Certificate> keyCertChain) {
    return keyManager(key, toArray(keyCertChain, EmptyArrays.EMPTY_X509_CERTIFICATES));
  }

  public SslContextBuilder keyManager(
      PrivateKey key, String keyPassword, X509Certificate... keyCertChain) {
    if (forServer) {
      Objects.requireNonNull(keyCertChain, "keyCertChain required for servers");
      if (keyCertChain.length == 0) {
        throw new IllegalArgumentException("keyCertChain must be non-empty");
      }
      Objects.requireNonNull(key, "key required for servers");
    }
    if (keyCertChain == null || keyCertChain.length == 0) {
      this.keyCertChain = null;
    } else {
      for (X509Certificate cert : keyCertChain) {
        if (cert == null) {
          throw new IllegalArgumentException("keyCertChain contains null entry");
        }
      }
      this.keyCertChain = keyCertChain.clone();
    }
    this.key = key;
    this.keyPassword = keyPassword;
    keyManagerFactory = null;
    return this;
  }

  public SslContextBuilder keyManager(
      PrivateKey key, String keyPassword, Iterable<? extends X509Certificate> keyCertChain) {
    return keyManager(key, keyPassword, toArray(keyCertChain, EmptyArrays.EMPTY_X509_CERTIFICATES));
  }

  public SslContextBuilder keyManager(KeyManagerFactory keyManagerFactory) {
    if (forServer) {
      Objects.requireNonNull(keyManagerFactory, "keyManagerFactory required for servers");
    }
    keyCertChain = null;
    key = null;
    keyPassword = null;
    this.keyManagerFactory = keyManagerFactory;
    return this;
  }

  public SslContextBuilder keyManager(KeyManager keyManager) {
    if (forServer) {
      Objects.requireNonNull(keyManager, "keyManager required for servers");
    }
    if (keyManager != null) {
      // this.keyManagerFactory = new KeyManagerFactoryWrapper(keyManager);
    } else {
      this.keyManagerFactory = null;
    }
    keyCertChain = null;
    key = null;
    keyPassword = null;
    return this;
  }

  public SslContextBuilder sessionCacheSize(long sessionCacheSize) {
    this.sessionCacheSize = sessionCacheSize;
    return this;
  }

  public SslContextBuilder sessionTimeout(long sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
    return this;
  }

  public SslContextBuilder protocols(String... protocols) {
    this.protocols = protocols == null ? null : protocols.clone();
    return this;
  }

  public SslContextBuilder protocols(Iterable<String> protocols) {
    return protocols(toArray(protocols, EmptyArrays.EMPTY_STRINGS));
  }

  public SslContextBuilder startTls(boolean startTls) {
    this.startTls = startTls;
    return this;
  }

  private static SSLContext newSSLContextClient(
      Provider sslContextProvider,
      X509Certificate[] trustCertCollection,
      TrustManagerFactory trustManagerFactory,
      X509Certificate[] keyCertChain,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      long sessionCacheSize,
      long sessionTimeout,
      String keyStore)
      throws SSLException {
    try {
      if (trustCertCollection != null) {
        trustManagerFactory =
            buildTrustManagerFactory(trustCertCollection, trustManagerFactory, keyStore);
      }
      if (keyCertChain != null) {
        keyManagerFactory =
            buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory, keyStore);
      }
      SSLContext ctx =
          sslContextProvider == null
              ? SSLContext.getInstance("TLS")
              : SSLContext.getInstance("TLS", sslContextProvider);
      ctx.init(
          keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(),
          trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers(),
          null);

      SSLSessionContext sessCtx = ctx.getClientSessionContext();
      if (sessionCacheSize > 0) {
        sessCtx.setSessionCacheSize((int) Math.min(sessionCacheSize, Integer.MAX_VALUE));
      }
      if (sessionTimeout > 0) {
        sessCtx.setSessionTimeout((int) Math.min(sessionTimeout, Integer.MAX_VALUE));
      }
      return ctx;
    } catch (Exception e) {
      if (e instanceof SSLException) {
        throw (SSLException) e;
      }
      throw new SSLException("failed to initialize the client-side SSL context", e);
    }
  }

  private static SSLContext newSSLContextServer(
      Provider sslContextProvider,
      X509Certificate[] trustCertCollection,
      TrustManagerFactory trustManagerFactory,
      X509Certificate[] keyCertChain,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory keyManagerFactory,
      long sessionCacheSize,
      long sessionTimeout,
      String keyStore)
      throws SSLException {
    if (key == null && keyManagerFactory == null) {
      throw new NullPointerException("key, keyManagerFactory");
    }

    try {
      if (trustCertCollection != null) {
        trustManagerFactory =
            buildTrustManagerFactory(trustCertCollection, trustManagerFactory, keyStore);
      }
      if (key != null) {
        keyManagerFactory =
            buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory, null);
      }

      // Initialize the SSLContext to work with our key managers.
      SSLContext ctx =
          sslContextProvider == null
              ? SSLContext.getInstance("TLS")
              : SSLContext.getInstance("TLS", sslContextProvider);
      ctx.init(
          keyManagerFactory.getKeyManagers(),
          trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers(),
          null);

      SSLSessionContext sessCtx = ctx.getServerSessionContext();
      if (sessionCacheSize > 0) {
        sessCtx.setSessionCacheSize((int) Math.min(sessionCacheSize, Integer.MAX_VALUE));
      }
      if (sessionTimeout > 0) {
        sessCtx.setSessionTimeout((int) Math.min(sessionTimeout, Integer.MAX_VALUE));
      }
      return ctx;
    } catch (Exception e) {
      if (e instanceof SSLException) {
        throw (SSLException) e;
      }
      throw new SSLException("failed to initialize the server-side SSL context", e);
    }
  }

  static TrustManagerFactory buildTrustManagerFactory(
      X509Certificate[] certCollection,
      TrustManagerFactory trustManagerFactory,
      String keyStoreType)
      throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
    if (keyStoreType == null) {
      keyStoreType = KeyStore.getDefaultType();
    }
    final KeyStore ks = KeyStore.getInstance(keyStoreType);
    ks.load(null, null);

    int i = 1;
    for (X509Certificate cert : certCollection) {
      String alias = Integer.toString(i);
      ks.setCertificateEntry(alias, cert);
      i++;
    }

    // Set up trust manager factory to use our key store.
    if (trustManagerFactory == null) {
      trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    }
    trustManagerFactory.init(ks);

    return trustManagerFactory;
  }

  static KeyManagerFactory buildKeyManagerFactory(
      X509Certificate[] certChain,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory kmf,
      String keyStoreType)
      throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
          CertificateException, IOException {
    return buildKeyManagerFactory(
        certChain, KeyManagerFactory.getDefaultAlgorithm(), key, keyPassword, kmf, keyStoreType);
  }

  static KeyManagerFactory buildKeyManagerFactory(
      X509Certificate[] certChainFile,
      String keyAlgorithm,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory kmf,
      String keyStore)
      throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
          UnrecoverableKeyException {
    char[] keyPasswordChars = keyStorePassword(keyPassword);
    KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars, keyStore);
    return buildKeyManagerFactory(ks, keyAlgorithm, keyPasswordChars, kmf);
  }

  static KeyManagerFactory buildKeyManagerFactory(
      X509Certificate[] certChainFile,
      String keyAlgorithm,
      PrivateKey key,
      String keyPassword,
      KeyManagerFactory kmf)
      throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
          UnrecoverableKeyException {
    char[] keyPasswordChars = keyStorePassword(keyPassword);
    KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars, KeyStore.getDefaultType());
    return buildKeyManagerFactory(ks, keyAlgorithm, keyPasswordChars, kmf);
  }

  static char[] keyStorePassword(String keyPassword) {
    return keyPassword == null ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
  }

  static KeyStore buildKeyStore(
      X509Certificate[] certChain, PrivateKey key, char[] keyPasswordChars, String keyStoreType)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    if (keyStoreType == null) {
      keyStoreType = KeyStore.getDefaultType();
    }
    KeyStore ks = KeyStore.getInstance(keyStoreType);
    ks.load(null, null);
    ks.setKeyEntry("key", key, keyPasswordChars, certChain);
    return ks;
  }

  static KeyManagerFactory buildKeyManagerFactory(
      KeyStore ks, String keyAlgorithm, char[] keyPasswordChars, KeyManagerFactory kmf)
      throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
    // Set up key manager factory to use our key store
    if (kmf == null) {
      kmf = KeyManagerFactory.getInstance(keyAlgorithm);
    }
    kmf.init(ks, keyPasswordChars);

    return kmf;
  }

  public SSLContext build() throws SSLException {
    if (forServer) {
      return newSSLContextServer(
          sslContextProvider,
          trustCertCollection,
          trustManagerFactory,
          keyCertChain,
          key,
          keyPassword,
          keyManagerFactory,
          sessionCacheSize,
          sessionTimeout,
          keyStoreType);
    } else {
      return newSSLContextClient(
          sslContextProvider,
          trustCertCollection,
          trustManagerFactory,
          keyCertChain,
          key,
          keyPassword,
          keyManagerFactory,
          sessionCacheSize,
          sessionTimeout,
          keyStoreType);
    }
  }

  private static <T> T[] toArray(Iterable<? extends T> iterable, T[] prototype) {
    if (iterable == null) {
      return null;
    }
    final List<T> list = new ArrayList<T>();
    for (T element : iterable) {
      list.add(element);
    }
    return list.toArray(prototype);
  }
}
