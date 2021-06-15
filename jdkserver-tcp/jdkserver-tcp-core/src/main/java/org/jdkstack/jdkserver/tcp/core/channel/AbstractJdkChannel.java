package org.jdkstack.jdkserver.tcp.core.channel;

import java.nio.channels.SelectionKey;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import org.study.network.codecs.NetworkMessage;

public abstract class AbstractJdkChannel implements JdkChannel {
  protected static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
  protected final ChannelPipeline pipeline = new DefaultChannelPipeline();
}
