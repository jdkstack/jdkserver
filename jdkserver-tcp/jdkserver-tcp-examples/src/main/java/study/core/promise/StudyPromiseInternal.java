package study.core.promise;

import study.core.future.StudyFutureInternal;

public interface StudyPromiseInternal<T>
    extends StudyPromise<T>, StudyFutureInternal<T> { // FutureListener<T>,
}
