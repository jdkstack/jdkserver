package org.jdkstack.jdkserver.http.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Exchange {

  Context getHttpContext();

  Header getResponseHeaders();

  OutputStream getResponseBody();

  void sendResponseHeaders(int rCode, long contentLen) throws IOException;

  InputStream getRequestBody();
}
