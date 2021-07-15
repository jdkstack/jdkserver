package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Iterator;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.jdkstack.jdkserver.tcp.core.core.channel.ClientMode;
import org.jdkstack.jdkserver.tcp.core.ssl.handler.SslHandler;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelInputStream;
import org.jdkstack.jdkserver.tcp.core.ssl.socket.SslSocketChannelOutputStream;

public class SSLHandshakeClient {

  private SocketChannel sc;
  private SSLEngine sslEngine;
  private Selector selector;
  private SslHandler sslStreams;
  private InputStream inputStream;
  private OutputStream outputStream;

  public void run() throws Exception {
    SocketChannel channel = SocketChannel.open();
    channel.socket().setTcpNoDelay(true);
    channel.configureBlocking(false);
    // 写缓冲区,影响写消息(目前读写消息时,出现写不出去,读不进来的现象).
    // channel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1000000);
    // 读缓冲区,影响读消息(目前读写消息时,出现写不出去,读不进来的现象).
    // channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1000000);
    selector = Selector.open();
    channel.register(selector, SelectionKey.OP_CONNECT);
    channel.connect(new InetSocketAddress("localhost", 443));

    while (true) {

      selector.select();
      Iterator<SelectionKey> it = selector.selectedKeys().iterator();
      while (it.hasNext()) {
        SelectionKey selectionKey = it.next();
        it.remove();
        handleSocketEvent(selectionKey);
      }
    }
  }

  /**
   * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as
   * an input.
   *
   * @param filepath - the path to the JKS keystore.
   * @param keystorePassword - the keystore's password.
   * @param keyPassword - the key's passsword.
   * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
   * @throws Exception
   */
  protected static KeyManager[] createKeyManagers(
      String filepath, String keystorePassword, String keyPassword) throws Exception {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    InputStream keyStoreIS = new FileInputStream(filepath);
    try {
      keyStore.load(keyStoreIS, keystorePassword.toCharArray());
    } finally {
      if (keyStoreIS != null) {
        keyStoreIS.close();
      }
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, keyPassword.toCharArray());
    return kmf.getKeyManagers();
  }

  /**
   * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore as
   * an input.
   *
   * @param filepath - the path to the JKS keystore.
   * @param keystorePassword - the keystore's password.
   * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
   * @throws Exception
   */
  protected static TrustManager[] createTrustManagers(String filepath, String keystorePassword)
      throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");
    InputStream trustStoreIS = new FileInputStream(filepath);
    try {
      trustStore.load(trustStoreIS, keystorePassword.toCharArray());
    } finally {
      if (trustStoreIS != null) {
        trustStoreIS.close();
      }
    }
    TrustManagerFactory trustFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustFactory.init(trustStore);
    return trustFactory.getTrustManagers();
  }

  private void handleSocketEvent(SelectionKey key) throws Exception {
    if (key.isConnectable()) {
      sc = (SocketChannel) key.channel();
      if (sc.isConnectionPending()) {
        sc.finishConnect();
      }
      sc.register(selector, SelectionKey.OP_READ);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(
          createKeyManagers("conf\\client.jks", "storepass", "keypass"),
          createTrustManagers("conf\\trustedCerts.jks", "storepass"),
          new SecureRandom());
      sslStreams = new SslHandler(context, sc, ClientMode.CLIENT);
      inputStream = new SslSocketChannelInputStream(sslStreams); // sslStreams.getInputStream();
      outputStream = new SslSocketChannelOutputStream(sslStreams); // sslStreams.getOutputStream();
      //  for (int i = 0; i < 10; i++) {
      try {
        outputStream.write("Im client?".getBytes(StandardCharsets.UTF_8));
        // outputStream.write("\r\n".getBytes());
        outputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      // }
    }
    if (key.isReadable()) {
      sc = (SocketChannel) key.channel();
      byte[] b = new byte[1024];
      inputStream.read(b);
      System.out.println(new String(b, StandardCharsets.UTF_8));
    }
  }

  public static void main(String[] args) {
    try {
      // System.setProperty("javax.net.debug", "all");
      new SSLHandshakeClient().run();
      Thread.sleep(99999);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
