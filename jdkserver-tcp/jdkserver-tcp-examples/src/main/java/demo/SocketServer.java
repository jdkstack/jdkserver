package demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Socket Server Example. */
public class SocketServer {

  /**
   * @param args ignored.
   * @throws IOException exception.
   */
  public static void main(final String[] args) throws IOException {
    final SocketServer server = new SocketServer();
    assert server != null;
  }

  private static final Logger LOGGER = Logger.getLogger(SocketServer.class.getName());

  /** The {@link ServerSocketChannel} we're listening to. */
  private final ServerSocketChannel serverChannel;
  /** The {@link Selector}. */
  private final Selector mSelector;
  /** The set of write interests. */
  // it seems that it's recommended to not change interests with multiple threads
  // so multiple other examples uses this indirection where the other thread registers
  // a hint to write by adding the relevant information to a list/set/etc., and then
  // the main thread where the select runs is the one picking this data and calling
  // the key.interestOps(xxx).
  private final Set<SocketChannel> mWriteInterests = new HashSet<SocketChannel>();

  /** @throws IOException exception. */
  public SocketServer() throws IOException {
    super();
    // listen to all 8080, good enough for testing
    final InetSocketAddress addr = new InetSocketAddress(8080);
    // open the socket channel
    this.serverChannel = ServerSocketChannel.open();
    // set it to non-blocking
    this.serverChannel.configureBlocking(false);
    // get the real socket
    final ServerSocket socket = this.serverChannel.socket();
    // set the socket to reuse so it doesn't hang on time-wait when the app is restarted
    socket.setReuseAddress(true);
    // create a selector
    this.mSelector = SelectorProvider.provider().openSelector();
    // register the socket into the selector, for accepting new connections
    this.serverChannel.register(this.mSelector, SelectionKey.OP_ACCEPT);
    // finally bind it to the address
    socket.bind(addr);
    if (SocketServer.LOGGER.isLoggable(Level.INFO))
      SocketServer.LOGGER.log(Level.INFO, socket + " started");

    // now loop!
    Iterator<SelectionKey> selectedKeys;
    SelectionKey key;
    while (true) {
      // set sockets to write if requested
      // this indirection is to ensure that the key.interestOps is
      // *never* called from a different thread
      synchronized (this.mWriteInterests) {
        for (final SocketChannel change : this.mWriteInterests) {
          // socket closed or invalid / null key may happen if
          // the connection is closed inbetween the other thread
          // pinging this and the selector cycling
          if (!change.isOpen()) continue;
          key = change.keyFor(this.mSelector);
          if (key == null || !key.isValid()) continue;
          key.interestOps(SelectionKey.OP_WRITE);
        }
        this.mWriteInterests.clear();
      }

      this.mSelector.select();

      selectedKeys = this.mSelector.selectedKeys().iterator();
      while (selectedKeys.hasNext()) {
        key = selectedKeys.next();
        selectedKeys.remove();
        if (!key.isValid()) continue;
        // it seems the key has only one interest (never read+write), so a elseif is better here
        if (key.isAcceptable()) this.accept(key);
        else if (key.isReadable()) this.read(key);
        else if (key.isWritable()) this.write(key);
      }
    }
  }

  private void accept(final SelectionKey key) throws IOException {
    // For an accept to be pending the channel must be a server socket channel.
    final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

    // Accept the connection and make it non-blocking
    final SocketChannel socketChannel = serverSocketChannel.accept(); // IOException
    socketChannel.configureBlocking(false); // IOException
    // get the socket and set it to no delay (so small packets don't hang on the OS's buffers
    final Socket socket = socketChannel.socket();
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    socket.setTrafficClass(0x08 | 0x10);

    final SocketControl socketControl =
        new SocketControl() {

          @SuppressWarnings("synthetic-access")
          @Override
          public void hintWrite() {
            SocketServer.this.hintWrite(socketChannel);
          }
        };

    // instead of a map of connections and workers, we attach the worker directly to the
    // key, thank you for the key.attach() api!
    final Worker worker = new Worker(socketControl);
    // Register the new SocketChannel with our Selector, indicating
    // we'd like to be notified when there's data waiting to be read
    socketChannel.register(this.mSelector, SelectionKey.OP_READ, worker);
  }

  private void hintWrite(final SocketChannel socket) {
    // only call the selector.wakeup once per cycle, kind of
    boolean changed;
    synchronized (this.mWriteInterests) {
      changed = this.mWriteInterests.add(socket);
    }
    if (changed) this.mSelector.wakeup();
  }

  private void read(final SelectionKey key) throws IOException {
    final SocketChannel socketChannel = (SocketChannel) key.channel();
    final Worker worker = ((Worker) key.attachment());
    final ByteBuffer buf = worker.getReadBuffer();
    if (buf.remaining() == 0) System.err.println("buffer full BAD BAD BAD!"); // XXX
    else {
      int numRead;
      try {
        numRead = socketChannel.read(buf);
      } catch (final IOException e) {
        this.disconnect(key);
        return;
      }
      if (numRead <= 0) {
        this.disconnect(key);
        return;
      }
      System.out.println("read " + numRead);
    }
    worker.parseReadBuffer();
  }

  private void write(final SelectionKey key) throws IOException {
    final SocketChannel socketChannel = (SocketChannel) key.channel();
    final Worker worker = ((Worker) key.attachment());
    int wrote = 0;
    ByteBuffer buf;
    try {
      // write all data until...
      while (true) {
        buf = worker.getWriteBuffer();
        if (buf == null || buf.remaining() == 0)
          // ...no more data
          break;
        wrote += socketChannel.write(buf);
        if (buf.remaining() > 0) {
          // ... or the socket's buffer fills up
          break;
        }
        // break;
      }
    } catch (final IOException e) {
      this.disconnect(key);
      return;
    }
    worker.parseWriteBuffer();
    // if no more data to write, switch it back to READ mode.
    if (buf == null || buf.remaining() == 0) key.interestOps(SelectionKey.OP_READ);
    System.out.println("wrote " + wrote + " hasmore=" + (buf != null && buf.remaining() != 0));
  }

  @SuppressWarnings("static-method")
  private void disconnect(final SelectionKey key) throws IOException {
    final SocketChannel socketChannel = (SocketChannel) key.channel();
    final Worker worker = (Worker) key.attachment();
    worker.disconnected();
    key.attach(null);
    // The remote forcibly closed the connection, cancel
    // the selection key and close the channel.
    key.cancel();
    key.channel().close();
    if (SocketServer.LOGGER.isLoggable(Level.INFO))
      SocketServer.LOGGER.log(
          Level.INFO, "[" + socketChannel.socket().getRemoteSocketAddress() + "] Disconnected");
  }

  private static interface SocketControl {

    /** Hint the selector that there are bytes to write. */
    void hintWrite();
  }

  private static class Worker {

    // the SocketControl to hint when there are bytes to write
    private final SocketControl mSocketControl;
    // queue of incoming data
    private final BlockingQueue<ByteBuffer> mQueueIn = new LinkedBlockingQueue<ByteBuffer>(1024);
    // queue of outgoing data
    private final BlockingQueue<ByteBuffer> mQueueOut = new LinkedBlockingQueue<ByteBuffer>(1024);
    // buffer for incoming data
    private final ByteBuffer mReadBuf = ByteBuffer.allocate(8192);
    // holder for outgoing data
    private ByteBuffer mWriteBuf;
    // thread for doing something with the data
    private Thread mThread;

    Worker(final SocketControl socketControl) {
      super();
      this.mSocketControl = socketControl;
      final Runnable r =
          new Runnable() {

            private final int test = 3;

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {
              while (true) {
                try {
                  while (true) {
                    switch (this.test) {
                      case 1:
                        {
                          // test case 1 simply writes back every incoming packet
                          // expected and result values between 50 and 80MB/s in and out
                          final ByteBuffer buf = Worker.this.mQueueIn.take();
                          Worker.this.mQueueOut.put(buf);
                          Worker.this.mSocketControl.hintWrite();
                          break;
                        }
                      case 2:
                        {
                          // test case 2 writes back every incoming packet as a double-sized packet
                          // expected values ?
                          // result failed read and write
                          final ByteBuffer buf = Worker.this.mQueueIn.take();
                          final ByteBuffer buf2 = ByteBuffer.allocate(buf.remaining() * 2);
                          buf2.put(buf);
                          buf.flip();
                          buf2.put(buf);
                          buf2.flip();
                          Worker.this.mQueueOut.put(buf2);
                          Worker.this.mSocketControl.hintWrite();
                          break;
                        }
                      case 3:
                        {
                          // test case 3 writes back every incoming packet as a two similar packets
                          // expected values ?
                          // result failed read and write
                          final ByteBuffer buf = Worker.this.mQueueIn.take();
                          final ByteBuffer buf2 = ByteBuffer.allocate(buf.remaining());
                          buf2.put(buf);
                          buf.flip();
                          buf2.flip();
                          Worker.this.mQueueOut.put(buf);
                          Worker.this.mSocketControl.hintWrite();
                          Worker.this.mQueueOut.put(buf2);
                          Worker.this.mSocketControl.hintWrite();
                          break;
                        }
                      case 4:
                        {
                          // test case 4 writes back every incoming packet as a half sized packets
                          // expected and result values between 50 and 80MB/s in and exactly half
                          // out
                          final ByteBuffer buf = Worker.this.mQueueIn.take();
                          final ByteBuffer buf2 = ByteBuffer.allocate(buf.remaining() / 2);
                          buf.limit(buf.remaining() / 2);
                          buf2.put(buf);
                          buf2.flip();
                          Worker.this.mQueueOut.put(buf2);
                          Worker.this.mSocketControl.hintWrite();
                          break;
                        }
                      default:
                        break;
                    }
                  }
                } catch (final Throwable t) {
                  if (t instanceof InterruptedException) return;
                  t.printStackTrace();
                }
              }
            }
          };
      this.mThread = new Thread(r);
      this.mThread.start();
    }

    ByteBuffer getReadBuffer() {
      return this.mReadBuf;
    }

    void parseReadBuffer() {
      // skip until the whole incoming buffer if full
      // each read will increase the .position() until it reaches .limit()
      // which in this case is the same as .capacity, and hence remaining==0
      // happens when position=limit=capacity
      if (this.mReadBuf.remaining() != 0) return;
      // flip switches position to 0 and limit to capacity, so we can read it
      this.mReadBuf.flip();
      // duplicate the bytebuffer
      final ByteBuffer bb = ByteBuffer.allocate(this.mReadBuf.remaining());
      // this will set bb and readbuf to position=limit=capacity
      bb.put(this.mReadBuf);
      bb.flip(); // set position to 0 so later it can be read
      // put the new buffer into the incoming queue
      if (this.mQueueIn.offer(bb))
        // reset the buffer to read more data
        this.mReadBuf.clear();
      else
        // here we don't reset, hoping the next read cycle will skip with
        // the other BAD BAD error, but maybe the queue will have space to
        // fit it and the code above will be sucessful
        System.err.println("queue full BAD BAD BAD!"); // XXX
    }

    ByteBuffer getWriteBuffer() {
      // if the writeBuf is null or completely writen, pick a new one from the queue
      if (this.mWriteBuf == null || this.mWriteBuf.remaining() == 0)
        this.mWriteBuf = this.mQueueOut.poll();
      return this.mWriteBuf;
    }

    void parseWriteBuffer() {
      // noop
    }

    void disconnected() {
      synchronized (this.mThread) {
        this.mThread.interrupt();
      }
    }
  }
}
