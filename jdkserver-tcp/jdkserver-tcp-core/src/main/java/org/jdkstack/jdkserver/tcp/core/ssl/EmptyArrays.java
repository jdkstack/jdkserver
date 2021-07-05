package org.jdkstack.jdkserver.tcp.core.ssl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class EmptyArrays {

  public static final int[] EMPTY_INTS = {};
  public static final byte[] EMPTY_BYTES = {};
  public static final char[] EMPTY_CHARS = {};
  public static final Object[] EMPTY_OBJECTS = {};
  public static final Class<?>[] EMPTY_CLASSES = {};
  public static final String[] EMPTY_STRINGS = {};
  public static final StackTraceElement[] EMPTY_STACK_TRACE = {};
  public static final ByteBuffer[] EMPTY_BYTE_BUFFERS = {};
  public static final Certificate[] EMPTY_CERTIFICATES = {};
  public static final X509Certificate[] EMPTY_X509_CERTIFICATES = {};
  public static final javax.security.cert.X509Certificate[] EMPTY_JAVAX_X509_CERTIFICATES = {};

  private EmptyArrays() {}

  public static void main(String[] args) throws Exception {
    System.setProperty("javax.net.debug", "all");
    SSLContext sc = SSLContext.getInstance("TLS");
    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
    X509TrustManager trustManager =
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(
              X509Certificate[] paramArrayOfX509Certificate, String paramString)
              throws CertificateException {}

          @Override
          public void checkServerTrusted(
              X509Certificate[] paramArrayOfX509Certificate, String paramString)
              throws CertificateException {}

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }
        };
    // 取消主机名验证
    System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
    sc.init(null, new TrustManager[] {trustManager}, null);
    HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).sslContext(sc).build();

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create("https://www.server2.com/")).GET().build();
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    System.out.println(response.statusCode());
    System.out.println(response.body());
  }
}
