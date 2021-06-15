package org.study.network.core.socket;

import java.util.Objects;
import org.study.core.future.Handler;

public class PumpImpl<T> implements Pump {
  private final ReadStream<T> readStream;
  private final WriteStream<T> writeStream;
  private final Handler<T> dataHandler;
  private final Handler<Void> drainHandler;
  private int pumped;

  public PumpImpl(ReadStream<T> rs, WriteStream<T> ws, int maxWriteQueueSize) {
    this(rs, ws);
    this.writeStream.setWriteQueueMaxSize(maxWriteQueueSize);
  }

  public PumpImpl(ReadStream<T> rs, WriteStream<T> ws) {
    Objects.requireNonNull(rs);
    Objects.requireNonNull(ws);
    this.readStream = rs;
    this.writeStream = ws;
    drainHandler = v -> readStream.resume();
    dataHandler =
        data -> {
          writeStream.write(data);
          incPumped();
          if (writeStream.writeQueueFull()) {
            readStream.pause();
            writeStream.drainHandler(drainHandler);
          }
        };
  }

  @Override
  public PumpImpl setWriteQueueMaxSize(int maxSize) {
    writeStream.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public PumpImpl start() {
    readStream.handler(dataHandler);
    return this;
  }

  @Override
  public PumpImpl stop() {
    writeStream.drainHandler(null);
    readStream.handler(null);
    return this;
  }

  @Override
  public synchronized int numberPumped() {
    return pumped;
  }

  private synchronized void incPumped() {
    pumped++;
  }
}
