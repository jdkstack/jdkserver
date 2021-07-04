package study.core.promise;

import study.core.future.AsyncResult;
import study.core.future.StudyFuture;

public class Examples {

  public static void main(String[] args) {

    StudyFuture<String> listen = listen();

    listen.onSuccess(event -> System.out.println("============>" + event));
    listen.onFailure(event -> System.out.println(event.getMessage()));

    listen.onComplete(
        (AsyncResult<String> ar) -> {
          if (ar.succeeded()) {
            String props = ar.result();
            System.out.println("File size = " + props);
          } else {
            System.out.println("Failure: " + ar.cause().getMessage());
          }
        });
  }

  public static StudyFuture<String> listen() {
    String abc = "123";
    StudyPromise<String> promise = new StudyPromiseImpl<>();
    if (true) {
      promise.complete(abc);
    } else {
      promise.fail("error");
    }
    return promise.future();
  }
}
