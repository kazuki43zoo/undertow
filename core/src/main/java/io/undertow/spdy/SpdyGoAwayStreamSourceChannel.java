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

package io.undertow.spdy;

import io.undertow.server.protocol.framed.AbstractFramedChannel;
import org.xnio.Pooled;

import java.nio.ByteBuffer;

/**
 * A SPDY Ping frame
 *
 * @author Stuart Douglas
 */
public class SpdyGoAwayStreamSourceChannel extends SpdyStreamSourceChannel {

    private final int status;
    private final int lastGoodStreamId;

    SpdyGoAwayStreamSourceChannel(AbstractFramedChannel<SpdyChannel, SpdyStreamSourceChannel, SpdyStreamSinkChannel> framedChannel, Pooled<ByteBuffer> data, long frameDataRemaining, int status, int lastGoodStreamId) {
        super(framedChannel, data, frameDataRemaining);
        this.status = status;
        this.lastGoodStreamId = lastGoodStreamId;
        lastFrame();
    }

    public int getStatus() {
        return status;
    }

    public int getLastGoodStreamId() {
        return lastGoodStreamId;
    }
}
