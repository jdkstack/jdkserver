package org.jdkstack.jdkserver.tcp.core.promise;

import org.jdkstack.jdkserver.tcp.core.future.StudyFutureInternal;

public interface StudyPromiseInternal<T>
    extends StudyPromise<T>, StudyFutureInternal<T> { // FutureListener<T>,
}
