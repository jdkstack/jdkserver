package org.study.network.core.common.exception;

public class StudyException extends RuntimeException {

  public StudyException(String message) {
    super(message);
  }

  public StudyException(String message, Throwable cause) {
    super(message, cause);
  }

  public StudyException(Throwable cause) {
    super(cause);
  }

  public StudyException(String message, boolean noStackTrace) {
    super(message, null, !noStackTrace, !noStackTrace);
  }

  public StudyException(String message, Throwable cause, boolean noStackTrace) {
    super(message, cause, !noStackTrace, !noStackTrace);
  }

  public StudyException(Throwable cause, boolean noStackTrace) {
    super(null, cause, !noStackTrace, !noStackTrace);
  }
}
