/**
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

import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.aws.AWSElasticCacheClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.impl.ElectionMemcachedSessionLocator;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.impl.LibmemcachedMemcachedSessionLocator;
import net.rubyeye.xmemcached.impl.PHPMemcacheSessionLocator;
import net.rubyeye.xmemcached.impl.RandomMemcachedSessionLocaltor;
import net.rubyeye.xmemcached.impl.RoundRobinMemcachedSessionLocator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Factory for the {@link MemcachedCacheManager} instances.
 *
 * @author Igor Bolic
 */
public class XMemcachedCacheManagerFactory extends MemcachedCacheManagerFactory {

    public XMemcachedCacheManagerFactory(MemcachedCacheProperties properties) {
        super(properties);
    }

    @Override
    IMemcachedClient memcachedClient() throws IOException {
        final List<InetSocketAddress> servers = properties.getServers();
        final MemcachedCacheProperties.Provider provider = properties.getProvider();
        final MemcachedCacheProperties.Protocol protocol = properties.getProtocol();

        final MemcachedClientBuilder builder = MemcachedCacheProperties.Provider.AWS.equals(provider) ?
                new AWSElasticCacheClientBuilder(servers) : new XMemcachedClientBuilder(servers);

        if (builder instanceof AWSElasticCacheClientBuilder) {
            ((AWSElasticCacheClientBuilder) builder)
                    .setPollConfigIntervalMs(properties.getServersRefreshInterval().toMillis());
        }
        builder.setOpTimeout(properties.getOperationTimeout().toMillis());
        builder.setCommandFactory(MemcachedCacheProperties.Protocol.BINARY.equals(protocol) ?
                new BinaryCommandFactory() : new TextCommandFactory());

        if (properties.getHashStrategy() != MemcachedCacheProperties.HashStrategy.STANDARD) {
            builder.setSessionLocator(getSessionLocator(properties.getHashStrategy()));
        }

        return new XMemcachedClient(builder.build());
    }

    private MemcachedSessionLocator getSessionLocator(MemcachedCacheProperties.HashStrategy hashStrategy) {
        switch (hashStrategy) {
            case LIBMEMCACHED:
                return new LibmemcachedMemcachedSessionLocator();
            case KETAMA:
                return new KetamaMemcachedSessionLocator();
            case PHP:
                return new PHPMemcacheSessionLocator();
            case ELECTION:
                return new ElectionMemcachedSessionLocator();
            case ROUNDROBIN:
                return new RoundRobinMemcachedSessionLocator();
            case RANDOM:
            default:
                return new RandomMemcachedSessionLocaltor();
        }
    }
}
