package org.jdkstack.jdkserver.http.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.jdkstack.jdkserver.http.core.service.HttpServer;
import org.jdkstack.jdkserver.http.core.service.HttpsServer;
import org.jdkstack.jdkserver.http.core.spi.HttpServerProvider;

public class DefaultHttpServerProvider extends HttpServerProvider {
    public HttpServer createHttpServer (InetSocketAddress addr, int backlog) throws IOException {
        return new HttpServerImpl(addr, backlog);
    }

    public HttpsServer createHttpsServer (InetSocketAddress addr, int backlog) throws IOException {
        return new HttpsServerImpl(addr, backlog);
    }
}
