package org.jdkstack.jdkserver.http.core.service;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import org.jdkstack.jdkserver.http.core.HttpsConfigurator;
import org.jdkstack.jdkserver.http.core.spi.HttpServerProvider;

/**
 * This class is an extension of {@link HttpServer} which provides
 * support for HTTPS. <p>
 * A HttpsServer must have an associated {@link HttpsConfigurator} object
 * which is used to establish the SSL configuration for the SSL connections.
 * <p>
 * All other configuration is the same as for HttpServer.
 * @since 1.6
 */

public abstract class HttpsServer extends HttpServer {

    /**
     */
    protected HttpsServer () {
    }

    /**
     * creates a HttpsServer instance which is initially not bound to any local address/port.
     * The HttpsServer is acquired from the currently installed {@link HttpServerProvider}
     * The server must be bound using {@link #bind(InetSocketAddress,int)} before it can be used.
     * The server must also have a HttpsConfigurator established with {@link #setHttpsConfigurator(HttpsConfigurator)}
     * @throws IOException
     */
    public static HttpsServer create () throws IOException {
        return create (null, 0);
    }

    /**
     * Create a <code>HttpsServer</code> instance which will bind to the
     * specified {@link InetSocketAddress} (IP address and port number)
     *
     * A maximum backlog can also be specified. This is the maximum number of
     * queued incoming connections to allow on the listening socket.
     * Queued TCP connections exceeding this limit may be rejected by the TCP implementation.
     * The HttpsServer is acquired from the currently installed {@link HttpServerProvider}
     * The server must have a HttpsConfigurator established with {@link #setHttpsConfigurator(HttpsConfigurator)}
     *
     * @param addr the address to listen on, if <code>null</code> then bind() must be called
     *  to set the address
     * @param backlog the socket backlog. If this value is less than or equal to zero,
     *          then a system default value is used.
     * @throws BindException if the server cannot bind to the requested address,
     *          or if the server is already bound.
     * @throws IOException
     */

    public static HttpsServer create (
        InetSocketAddress addr, int backlog
    ) throws IOException {
        HttpServerProvider provider = HttpServerProvider.provider();
        return provider.createHttpsServer (addr, backlog);
    }

    /**
     * Sets this server's {@link HttpsConfigurator} object.
     * @param config the HttpsConfigurator to set
     * @throws NullPointerException if config is null.
     */
    public abstract void setHttpsConfigurator (HttpsConfigurator config) ;

    /**
     * Gets this server's {@link HttpsConfigurator} object, if it has been set.
     * @return the HttpsConfigurator for this server, or <code>null</code> if not set.
     */
    public abstract HttpsConfigurator getHttpsConfigurator ();
}
