package org.jdkstack.jdkserver.tcp.core.api.context;

public interface StudyThreadImpl {

  String getName();

  long startTime();

  long maxExecTime();

  StackTraceElement[] getStackTrace();
}
