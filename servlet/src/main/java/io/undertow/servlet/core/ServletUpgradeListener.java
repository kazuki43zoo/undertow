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

package io.undertow.servlet.core;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.HttpUpgradeListener;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.spec.WebConnectionImpl;
import org.xnio.ChannelListener;
import org.xnio.StreamConnection;

import javax.servlet.http.HttpUpgradeHandler;

/**
 * Lister that handles a servlet exchange upgrade event.
 *
 * @author Stuart Douglas
 */
public class ServletUpgradeListener<T extends HttpUpgradeHandler> implements HttpUpgradeListener {
    private final InstanceHandle<T> instance;
    private final ThreadSetupAction threadSetupAction;
    private final HttpServerExchange exchange;

    public ServletUpgradeListener(final InstanceHandle<T> instance, ThreadSetupAction threadSetupAction, HttpServerExchange exchange) {
        this.instance = instance;
        this.threadSetupAction = threadSetupAction;
        this.exchange = exchange;
    }

    @Override
    public void handleUpgrade(final StreamConnection channel, HttpServerExchange exchange) {
        channel.getCloseSetter().set(new ChannelListener<StreamConnection>() {
            @Override
            public void handleEvent(StreamConnection channel) {
                final ThreadSetupAction.Handle handle = threadSetupAction.setup(ServletUpgradeListener.this.exchange);
                try {
                    instance.getInstance().destroy();
                } finally {
                    try {
                        handle.tearDown();
                    } finally {
                        instance.release();
                    }
                }
            }
        });
        this.exchange.getIoThread().execute(new Runnable() {
            @Override
            public void run() {
                final ThreadSetupAction.Handle handle = threadSetupAction.setup(ServletUpgradeListener.this.exchange);
                try {
                    //run the upgrade in the IO thread, to prevent threading issues
                    instance.getInstance().init(new WebConnectionImpl(channel, ServletUpgradeListener.this.exchange.getConnection().getBufferPool()));
                } finally {
                    handle.tearDown();
                }
            }
        });
    }
}
