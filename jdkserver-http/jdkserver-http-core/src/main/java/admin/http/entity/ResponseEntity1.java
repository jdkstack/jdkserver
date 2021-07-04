package admin.http.entity;

import admin.http.status.HttpStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应entity1
 *
 * @author admin
 */
public class ResponseEntity1<T> {
  private Map<String, String> headers = new HashMap<>(16);
  private HttpStatus status;
  private T body;

  public ResponseEntity1(final HttpStatus status, final Map<String, String> headers, final T body) {
    this.status = status;
    this.headers = headers;
    this.body = body;
  }

  public ResponseEntity1(final HttpStatus status) {
    this.status = status;
  }

  public ResponseEntity1(final T body) {
    this.body = body;
  }

  public ResponseEntity1(final HttpStatus status, final T body) {
    this.status = status;
    this.body = body;
  }
}
