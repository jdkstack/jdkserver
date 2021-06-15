package org.study.core.future;

public interface StudyFutureInternal<T> extends StudyFuture<T> {

  void addListener(Listener<T> listener);
}
