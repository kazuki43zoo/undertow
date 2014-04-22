/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.undertow.server.protocol.ajp;

import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;
import io.undertow.server.HttpHandler;
import io.undertow.server.OpenListener;
import org.xnio.OptionMap;
import org.xnio.Pool;
import org.xnio.StreamConnection;

import java.nio.ByteBuffer;

import static io.undertow.UndertowOptions.DECODE_URL;
import static io.undertow.UndertowOptions.URL_CHARSET;

/**
 * @author Stuart Douglas
 */
public class AjpOpenListener implements OpenListener {

    public static final String UTF_8 = "UTF-8";
    private final Pool<ByteBuffer> bufferPool;
    private final int bufferSize;

    private volatile String scheme;

    private volatile HttpHandler rootHandler;

    private volatile OptionMap undertowOptions;

    private final AjpRequestParser parser;

    public AjpOpenListener(final Pool<ByteBuffer> pool, final int bufferSize) {
        this(pool, OptionMap.EMPTY, bufferSize);
    }

    public AjpOpenListener(final Pool<ByteBuffer> pool, final OptionMap undertowOptions, final int bufferSize) {
        this.undertowOptions = undertowOptions;
        this.bufferPool = pool;
        this.bufferSize = bufferSize;
        parser = new AjpRequestParser(undertowOptions.get(URL_CHARSET, UTF_8), undertowOptions.get(DECODE_URL, true));
    }

    public void handleEvent(final StreamConnection channel) {
        if (UndertowLogger.REQUEST_LOGGER.isTraceEnabled()) {
            UndertowLogger.REQUEST_LOGGER.tracef("Opened connection with %s", channel.getPeerAddress());
        }

        AjpServerConnection connection = new AjpServerConnection(channel, bufferPool, rootHandler, undertowOptions, bufferSize);
        AjpReadListener readListener = new AjpReadListener(connection, scheme, parser);
        connection.setAjpReadListener(readListener);
        readListener.startRequest();
        channel.getSourceChannel().setReadListener(readListener);
        readListener.handleEvent(channel.getSourceChannel());
    }

    public HttpHandler getRootHandler() {
        return rootHandler;
    }

    public void setRootHandler(final HttpHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    public OptionMap getUndertowOptions() {
        return undertowOptions;
    }

    public void setUndertowOptions(final OptionMap undertowOptions) {
        if (undertowOptions == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("undertowOptions");
        }
        this.undertowOptions = undertowOptions;
    }

    @Override
    public Pool<ByteBuffer> getBufferPool() {
        return bufferPool;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }
}
