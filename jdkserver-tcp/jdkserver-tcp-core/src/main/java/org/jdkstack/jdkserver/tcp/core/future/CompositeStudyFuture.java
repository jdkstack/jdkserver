package org.jdkstack.jdkserver.tcp.core.future;

import java.util.ArrayList;
import java.util.List;

public interface CompositeStudyFuture extends StudyFuture<CompositeStudyFuture> {

  static <T1, T2> CompositeStudyFuture all(StudyFuture<T1> f1, StudyFuture<T2> f2) {
    return CompositeStudyFutureImpl.all(f1, f2);
  }

  static <T1, T2, T3> CompositeStudyFuture all(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3) {
    return CompositeStudyFutureImpl.all(f1, f2, f3);
  }

  static <T1, T2, T3, T4> CompositeStudyFuture all(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3, StudyFuture<T4> f4) {
    return CompositeStudyFutureImpl.all(f1, f2, f3, f4);
  }

  static <T1, T2, T3, T4, T5> CompositeStudyFuture all(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5) {
    return CompositeStudyFutureImpl.all(f1, f2, f3, f4, f5);
  }

  static <T1, T2, T3, T4, T5, T6> CompositeStudyFuture all(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5,
      StudyFuture<T6> f6) {
    return CompositeStudyFutureImpl.all(f1, f2, f3, f4, f5, f6);
  }

  static CompositeStudyFuture all(List<StudyFuture> futures) {
    return CompositeStudyFutureImpl.all(futures.toArray(new StudyFuture[0]));
  }

  static <T1, T2> CompositeStudyFuture any(StudyFuture<T1> f1, StudyFuture<T2> f2) {
    return CompositeStudyFutureImpl.any(f1, f2);
  }

  static <T1, T2, T3> CompositeStudyFuture any(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3) {
    return CompositeStudyFutureImpl.any(f1, f2, f3);
  }

  static <T1, T2, T3, T4> CompositeStudyFuture any(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3, StudyFuture<T4> f4) {
    return CompositeStudyFutureImpl.any(f1, f2, f3, f4);
  }

  static <T1, T2, T3, T4, T5> CompositeStudyFuture any(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5) {
    return CompositeStudyFutureImpl.any(f1, f2, f3, f4, f5);
  }

  static <T1, T2, T3, T4, T5, T6> CompositeStudyFuture any(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5,
      StudyFuture<T6> f6) {
    return CompositeStudyFutureImpl.any(f1, f2, f3, f4, f5, f6);
  }

  static CompositeStudyFuture any(List<StudyFuture> futures) {
    return CompositeStudyFutureImpl.any(futures.toArray(new StudyFuture[0]));
  }

  static <T1, T2> CompositeStudyFuture join(StudyFuture<T1> f1, StudyFuture<T2> f2) {
    return CompositeStudyFutureImpl.join(f1, f2);
  }

  static <T1, T2, T3> CompositeStudyFuture join(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3) {
    return CompositeStudyFutureImpl.join(f1, f2, f3);
  }

  static <T1, T2, T3, T4> CompositeStudyFuture join(
      StudyFuture<T1> f1, StudyFuture<T2> f2, StudyFuture<T3> f3, StudyFuture<T4> f4) {
    return CompositeStudyFutureImpl.join(f1, f2, f3, f4);
  }

  static <T1, T2, T3, T4, T5> CompositeStudyFuture join(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5) {
    return CompositeStudyFutureImpl.join(f1, f2, f3, f4, f5);
  }

  static <T1, T2, T3, T4, T5, T6> CompositeStudyFuture join(
      StudyFuture<T1> f1,
      StudyFuture<T2> f2,
      StudyFuture<T3> f3,
      StudyFuture<T4> f4,
      StudyFuture<T5> f5,
      StudyFuture<T6> f6) {
    return CompositeStudyFutureImpl.join(f1, f2, f3, f4, f5, f6);
  }

  static CompositeStudyFuture join(List<StudyFuture> futures) {
    return CompositeStudyFutureImpl.join(futures.toArray(new StudyFuture[0]));
  }

  @Override
  CompositeStudyFuture onComplete(Handler<AsyncResult<CompositeStudyFuture>> handler);

  @Override
  default CompositeStudyFuture onSuccess(Handler<CompositeStudyFuture> handler) {
    StudyFuture.super.onSuccess(handler);
    return this;
  }

  @Override
  default CompositeStudyFuture onFailure(Handler<Throwable> handler) {
    StudyFuture.super.onFailure(handler);
    return this;
  }

  Throwable cause(int index);

  boolean succeeded(int index);

  boolean failed(int index);

  boolean isComplete(int index);

  <T> T resultAt(int index);

  int size();

  default <T> List<T> list() {
    int size = size();
    ArrayList<T> list = new ArrayList<>(size);
    for (int index = 0; index < size; index++) {
      list.add(resultAt(index));
    }
    return list;
  }

  default List<Throwable> causes() {
    int size = size();
    ArrayList<Throwable> list = new ArrayList<>(size);
    for (int index = 0; index < size; index++) {
      list.add(cause(index));
    }
    return list;
  }
}
