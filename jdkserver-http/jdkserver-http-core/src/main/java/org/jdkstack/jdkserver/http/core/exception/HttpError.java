package org.jdkstack.jdkserver.http.core.exception;

/** A Http error */
public class HttpError extends RuntimeException {
  private static final long serialVersionUID = 8769596371344178179L;

  public HttpError(String msg) {
    super(msg);
  }
}
