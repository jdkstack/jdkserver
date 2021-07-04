package study.network.core.socket;

public interface Pump {

  static <T> Pump pump(ReadStream<T> rs, WriteStream<T> ws) {
    return new PumpImpl<>(rs, ws);
  }

  static <T> Pump pump(ReadStream<T> rs, WriteStream<T> ws, int writeQueueMaxSize) {
    return new PumpImpl<>(rs, ws, writeQueueMaxSize);
  }

  Pump setWriteQueueMaxSize(int maxSize);

  Pump start();

  Pump stop();

  int numberPumped();
}
