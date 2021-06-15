package org.study.core.promise;

import org.study.core.future.StudyFutureInternal;

public interface StudyPromiseInternal<T>
    extends StudyPromise<T>, StudyFutureInternal<T> { // FutureListener<T>,
}
