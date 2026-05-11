/*
 * Copyright 2016-2026 Sixhours
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

import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SpyMemcachedClientTest {

    private final MemcachedClient client = mock(MemcachedClient.class);

    private SpyMemcachedClient memcachedClient;

    @BeforeEach
    void setUp() {
        this.memcachedClient = new SpyMemcachedClient(client);

        given(client.get(anyString())).willReturn("result");
        given(client.incr(anyString(), anyLong())).willReturn(123L);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(client);
    }

    @Test
    void whenGetNativeCache_thenReturnCorrectValue() {
        MemcachedClient result = this.memcachedClient.nativeClient();

        assertThat(result).isNotNull();
    }

    @Test
    void whenGet_thenCorrectMethodInvoked() {
        Object result = memcachedClient.get("my-key");

        assertThat(result).isNotNull();
        verify(client).get("my-key");
    }

    @Test
    void whenSet_thenCorrectMethodInvoked() {
        memcachedClient.set("my-key", 12000, "my-value");

        verify(client).set("my-key", 12000, "my-value");
    }

    @Test
    void whenTouch_thenCorrectMethodInvoked() throws ExecutionException, InterruptedException {
        memcachedClient.touch("my-key", 700);

        verify(client).touch("my-key", 700);
    }

    @Test
    void whenDelete_thenCorrectMethodInvoked() {
        memcachedClient.delete("my-key");

        verify(client).delete("my-key");
    }

    @Test
    void whenFlush_thenCorrectMethodInvoked() {
        memcachedClient.flush();

        verify(client).flush();
    }

    @Test
    void whenIncr_thenCorrectMethodInvoked() {
        memcachedClient.incr("my-key", 2);

        verify(client).incr("my-key", 2);
    }

    @Test
    void whenShutdown_thenCorrectMethodInvoked() {
        memcachedClient.shutdown();

        verify(client).shutdown();
    }
}