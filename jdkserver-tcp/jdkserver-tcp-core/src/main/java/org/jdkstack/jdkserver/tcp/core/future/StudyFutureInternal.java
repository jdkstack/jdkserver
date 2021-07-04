package org.jdkstack.jdkserver.tcp.core.future;

public interface StudyFutureInternal<T> extends StudyFuture<T> {

  void addListener(Listener<T> listener);
}
