package org.study.network.core.socket;

import io.netty.channel.ChannelHandlerContext;
import org.study.core.future.AsyncResult;
import org.study.core.future.Handler;
import org.study.core.future.StudyFuture;

public interface NetSocketInternal extends NetSocket {

  ChannelHandlerContext channelHandlerContext();

  StudyFuture<Void> writeMessage(Object message);

  NetSocketInternal writeMessage(Object message, Handler<AsyncResult<Void>> handler);

  NetSocketInternal messageHandler(Handler<Object> handler);
}
