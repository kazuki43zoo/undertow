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

package io.undertow.websockets.jsr.test.annotated;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

/**
 * @author Stuart Douglas
 */
public class EncodableObject {

    private final String value;

    public EncodableObject(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static class Encoder implements javax.websocket.Encoder.Text<EncodableObject> {

        boolean initalized = false;
        public static volatile boolean destroyed = false;

        @Override
        public String encode(final EncodableObject object) throws EncodeException {
            if (!initalized) {
                return "not initialized";
            }
            return object.value;
        }

        @Override
        public void init(final EndpointConfig config) {
            initalized = true;
        }

        @Override
        public void destroy() {

        }
    }

    public static class Decoder implements javax.websocket.Decoder.Text<EncodableObject> {

        boolean initalized = false;
        public static volatile boolean destroyed = false;

        @Override
        public void init(final EndpointConfig config) {
            initalized = true;
        }

        @Override
        public void destroy() {
            destroyed = true;
        }

        @Override
        public EncodableObject decode(final String s) throws DecodeException {
            if(!initalized) {
                throw new DecodeException(s, "not initialized");
            }
            return new EncodableObject(s);
        }

        @Override
        public boolean willDecode(final String s) {
            return true;
        }
    }
}
