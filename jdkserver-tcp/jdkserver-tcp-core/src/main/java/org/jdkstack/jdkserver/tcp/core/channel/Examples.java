package org.jdkstack.jdkserver.tcp.core.channel;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;
import org.study.core.context.Monitor;
import org.study.core.context.StudyThreadFactory;
import org.study.core.context.ThreadMonitor;
import org.study.network.core.common.option.Constants;

public class Examples {

  public static void main(String[] args) throws Exception {
    Monitor checker = new ThreadMonitor(Constants.BLOCK_TIME);
    ThreadFactory masterThreadFactory = new StudyThreadFactory(Constants.STUDY_MASTER, checker);
    ThreadFactory studyThreadFactory = new StudyThreadFactory(Constants.STUDY_, checker);

    JdkEventLoopGroup studyEventLoopGroup = new JdkEventLoopGroup(8, studyThreadFactory);
    studyEventLoopGroup.setIoRatio(50);

    JdkEventLoopGroup masterEventLoopGroup = new JdkEventLoopGroup(1, masterThreadFactory);
    masterEventLoopGroup.setIoRatio(100);
    SelectorProvider provider = SelectorProvider.provider();
    Selector selector = provider.openSelector();
    ServerSocketChannel ssc = ServerSocketChannel.open();
    SelectionKey selectionKey = ssc.register(selector, 0);

  }
}
