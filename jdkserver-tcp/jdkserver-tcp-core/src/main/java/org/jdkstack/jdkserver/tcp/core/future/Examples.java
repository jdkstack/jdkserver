package org.jdkstack.jdkserver.tcp.core.future;

public class Examples {

  public static void main(String[] args) {

    StudyFuture<String> listen = new StudyFutureImpl<>();
    // listen.tryComplete("true");
    listen.tryFail(new RuntimeException("FUCK"));

    listen.onSuccess(
        new Handler<String>() {
          @Override
          public void handle(String event) {
            System.out.println("============>" + event);
          }
        });
    listen.onFailure(
        new Handler<Throwable>() {
          @Override
          public void handle(Throwable event) {
            System.out.println(event.getMessage());
          }
        });

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
}
