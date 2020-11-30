/*
 * Copyright 2016-2020 Sixhours
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sixhours.memcached.cache;

import java.net.InetSocketAddress;

/**
 * {@link InetSocketAddress} wrapper class.
 *
 * @author Igor Bolic
 */
public final class SocketAddress {

    private final InetSocketAddress value;

    public SocketAddress(String value) {
        this.value = socketAddress(value);
    }

    public InetSocketAddress value() {
        return value;
    }

    /**
     * Validates and creates socket address value. Based on XMemcached's {@link net.rubyeye.xmemcached.utils.AddrUtil#getOneAddress(String)}.
     *
     * @param server Server address
     * @return InetSocketAddress
     */
    private InetSocketAddress socketAddress(String server) {
        if (server == null) {
            throw new IllegalArgumentException("Invalid server value. It should not be null");
        }
        if (server.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid server value. It cannot be empty");
        }
        server = server.trim();
        int finalColon = server.lastIndexOf(':');
        if (finalColon < 1) {
            throw new IllegalArgumentException("Invalid server value '" + server + "'");

        }

        final String hostPart = server.substring(0, finalColon).trim();
        final String portNum = server.substring(finalColon + 1).trim();

        return new InetSocketAddress(hostPart, Integer.parseInt(portNum));
    }
}
