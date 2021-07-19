package demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

public class NioEchoClient {
  public static void main(String... arg) {
    NioEchoServer nes = null; // use a free port.
    try {
      nes = new NioEchoServer(0);

    final SocketChannel sc = SocketChannel.open(new InetSocketAddress("localhost", nes.getPort()));
    sc.configureBlocking(false);
    // send data for 2 seconds.
    long writeCount = 0;
    final AtomicLong readCount = new AtomicLong();
    long start = System.currentTimeMillis();
    long end = start + 20000;
    Thread reader =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024);
                bb.clear();
                int read;
                try {
                  while ((read = sc.read(bb)) > 0) {
                    bb.clear();
                    readCount.addAndGet(read);
                  }
                } catch (IOException ignored) {
                }
              }
            });
    reader.start();
    ByteBuffer bb = ByteBuffer.allocateDirect(64 * 1024);
    while (end > System.currentTimeMillis()) {
      bb.clear();
      int write = sc.write(bb);

      if (write < 0) throw new AssertionError("Unexpected disconnection?");

      while (write == 0) {
        write = sc.write(bb);

        System.out.println("返回:"+write);
      }
      writeCount += write;
    }
    sc.shutdownOutput();
    reader.join();
    long time = System.currentTimeMillis() - start;
    System.out.printf(
        "Wrote: %,d bytes and read: %,d bytes in %,d ms%n", writeCount, readCount.get(), time);
    sc.close();
    nes.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
