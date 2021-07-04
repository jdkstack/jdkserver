package study.core.future;

public class NoStackTraceThrowable extends Throwable {

  public NoStackTraceThrowable(String message) {
    super(message, null, false, false);
  }
}
