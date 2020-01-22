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

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AppEngineMemcachedClientTest {

    public static final MemcacheService.IdentifiableValue IDENTIFIABLE_VALUE = () -> "identifiable-value";

    private final MemcacheService service = mock(MemcacheService.class);

    private AppEngineMemcachedClient memcachedClient;

    @Before
    public void setUp() {
        this.memcachedClient = new AppEngineMemcachedClient(service);

        given(service.get(any())).willReturn(CompletableFuture.completedFuture("result"));
        given(service.getIdentifiable(any())).willReturn(IDENTIFIABLE_VALUE);
        given(service.increment(any(), anyLong())).willReturn(123L);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(service);
    }

    @Test
    public void whenGetNativeCache_thenReturnCorrectValue() {
        Object result = memcachedClient.nativeCache();

        assertThat(result).isInstanceOf(MemcacheService.class);
    }

    @Test
    public void whenGet_thenCorrectMethodInvoked() {
        Object result = memcachedClient.get("my-key");

        assertThat(result).isNotNull();
        verify(service).get("my-key");
    }

    @Test
    public void whenSet_thenCorrectMethodInvoked() {
        memcachedClient.set("my-key", 12000, "my-value");

        verify(service).put("my-key", "my-value", Expiration.byDeltaSeconds(12000));
    }

    @Test
    public void whenTouch_thenCorrectMethodInvoked() throws ExecutionException, InterruptedException {
        memcachedClient.touch("my-key", 700);

        verify(service).getIdentifiable("my-key");
        verify(service).putIfUntouched("my-key", IDENTIFIABLE_VALUE, IDENTIFIABLE_VALUE.getValue(), Expiration.byDeltaSeconds(700));
    }

    @Test
    public void whenDelete_thenCorrectMethodInvoked() {
        memcachedClient.delete("my-key");

        verify(service).delete("my-key");
    }

    @Test
    public void whenFlush_thenCorrectMethodInvoked() {
        memcachedClient.flush();

        verify(service).clearAll();
    }

    @Test
    public void whenIncr_thenCorrectMethodInvoked() {
        memcachedClient.incr("my-key", 2);

        verify(service).increment("my-key", 2);
    }

    @Test
    public void whenShutdown_thenCorrectMethodInvoked() {
        memcachedClient.shutdown();

        verifyNoInteractions(service);
    }
}