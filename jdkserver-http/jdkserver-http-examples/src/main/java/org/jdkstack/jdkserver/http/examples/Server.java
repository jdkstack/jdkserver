package org.jdkstack.jdkserver.http.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.jdkstack.jdkserver.http.core.HttpsConfigurator;
import org.jdkstack.jdkserver.http.core.HttpsParameters;
import org.jdkstack.jdkserver.http.core.option.KeystoreOptions;
import org.jdkstack.jdkserver.http.core.option.TrustKeystoreOptions;
import org.jdkstack.jdkserver.http.core.service.HttpsServer;
import org.jdkstack.jdkserver.http.core.spi.HttpServerProvider;
import org.jdkstack.jdkserver.http.core.ssl.cert.CertManager;

public class Server {
  public static void main(String[] args) throws IOException, InterruptedException {
    try {
      System.setProperty("javax.net.debug", "all");
      KeystoreOptions keystoreOptions = new KeystoreOptions("xxxxxx", "PKCS12", "/cert/server");
      TrustKeystoreOptions trustKeystoreOptions =
          new TrustKeystoreOptions("xxxxxx", "PKCS12", "/cert/server-t");
      CertManager.parserKeystore(keystoreOptions);
      CertManager.parserTrustKeystore(trustKeystoreOptions);
      CertManager.initServerContext();
      HttpServerProvider httpServerProvider = HttpServerProvider.provider();
      HttpsServer httpserver =
          httpServerProvider.createHttpsServer(new InetSocketAddress("fe80::3448:6e02:8899:6e35",443), 100);
      SSLContext sslContext1 = CertManager.sslContextMap("serverManagerFactory");
      httpserver.setHttpsConfigurator(
          new HttpsConfigurator(sslContext1) {
            @Override
            public void configure(HttpsParameters params) {
              SNIMatcher matcher = SNIHostName.createSNIMatcher("(www.server.com|www.server2.com)");
              Collection<SNIMatcher> matchers = new ArrayList<>(1);
              matchers.add(matcher);
              String[] protocols = {"TLSv1.3"};
              params.setProtocols(protocols);
              SSLParameters sslParameters1 = new SSLParameters();
              sslParameters1.setSNIMatchers(matchers);
              params.setSSLParameters(sslParameters1);
            }
          });
      httpserver.createContext("/", CertManager.createServerSslHandler());
      httpserver.setExecutor(null);
      httpserver.start();
      System.out.println("server started");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
