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

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class XMemcachedClientTest {

    private final MemcachedClient client = mock(MemcachedClient.class);

    private XMemcachedClient memcachedClient;

    @Before
    public void setUp() throws InterruptedException, MemcachedException, TimeoutException {
        this.memcachedClient = new XMemcachedClient(client);

        given(client.get(anyString())).willReturn("result");
        given(client.incr(anyString(), anyLong())).willReturn(123L);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(client);
    }

    @Test
    public void whenGetNativeCache_thenReturnCorrectValue() {
        MemcachedClient result = this.memcachedClient.nativeClient();

        assertThat(result).isNotNull();
    }

    @Test
    public void whenGet_thenCorrectMethodInvoked() throws InterruptedException, MemcachedException, TimeoutException {
        Object result = memcachedClient.get("my-key");

        assertThat(result).isNotNull();
        verify(client).get("my-key");
    }

    @Test
    public void whenGetWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.get(anyString())).willThrow(new TimeoutException("Test timeout error"));

        assertThatThrownBy(() -> memcachedClient.get("my-key"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to get key")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).get("my-key");
    }

    @Test
    public void whenGetWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.get(anyString())).willThrow(new InterruptedException("Test interrupted error"));

        assertThatThrownBy(() -> memcachedClient.get("my-key"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to get key")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).get("my-key");
    }

    @Test
    public void whenSet_thenCorrectMethodInvoked() throws InterruptedException, MemcachedException, TimeoutException {
        memcachedClient.set("my-key", 12000, "my-value");

        verify(client).set("my-key", 12000, "my-value");
    }

    @Test
    public void whenSetWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.set(anyString(), anyInt(), any())).willThrow(new TimeoutException("Test timeout error"));

        assertThatThrownBy(() -> memcachedClient.set("my-key", 12000, "my-value"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to set key")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).set("my-key", 12000, "my-value");
    }

    @Test
    public void whenSetWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.set(anyString(), anyInt(), any())).willThrow(new InterruptedException("Test interrupted error"));

        assertThatThrownBy(() -> memcachedClient.set("my-key", 12000, "my-value"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to set key")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).set("my-key", 12000, "my-value");
    }

    @Test
    public void whenTouch_thenCorrectMethodInvoked() throws InterruptedException, TimeoutException, MemcachedException {
        memcachedClient.touch("my-key", 700);

        verify(client).touch("my-key", 700);
    }

    @Test
    public void whenTouchWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.touch(anyString(), anyInt())).willThrow(new TimeoutException("Test timeout error"));

        assertThatThrownBy(() -> memcachedClient.touch("my-key", 700))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to touch key")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).touch("my-key", 700);
    }

    @Test
    public void whenTouchWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.touch(anyString(), anyInt())).willThrow(new InterruptedException("Test interrupted error"));

        assertThatThrownBy(() -> memcachedClient.touch("my-key", 700))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to touch key")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).touch("my-key", 700);
    }

    @Test
    public void whenDelete_thenCorrectMethodInvoked() throws InterruptedException, MemcachedException, TimeoutException {
        memcachedClient.delete("my-key");

        verify(client).delete("my-key");
    }

    @Test
    public void whenDeleteWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.delete(anyString())).willThrow(new TimeoutException("Test timeout error"));

        assertThatThrownBy(() -> memcachedClient.delete("my-key"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to delete key")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).delete("my-key");
    }

    @Test
    public void whenDeleteWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.delete(anyString())).willThrow(new InterruptedException("Test interrupted error"));

        assertThatThrownBy(() -> memcachedClient.delete("my-key"))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to delete key")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).delete("my-key");
    }

    @Test
    public void whenFlush_thenCorrectMethodInvoked() throws InterruptedException, MemcachedException, TimeoutException {
        memcachedClient.flush();

        verify(client).flushAll();
    }

    @Test
    public void whenFlushWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        doThrow(new TimeoutException("Test timeout error")).when(client).flushAll();

        assertThatThrownBy(() -> memcachedClient.flush())
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to flush all keys")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).flushAll();
    }

    @Test
    public void whenFlushWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        doThrow(new InterruptedException("Test interrupted error")).when(client).flushAll();

        assertThatThrownBy(() -> memcachedClient.flush())
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to flush all keys")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).flushAll();
    }

    @Test
    public void whenIncr_thenCorrectMethodInvoked() throws InterruptedException, MemcachedException, TimeoutException {
        memcachedClient.incr("my-key", 2);

        verify(client).incr("my-key", 2);
    }

    @Test
    public void whenIncrWithError_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.incr(anyString(), anyLong())).willThrow(new TimeoutException("Test timeout error"));

        assertThatThrownBy(() -> memcachedClient.incr("my-key", 2))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to increment key")
                .hasCauseInstanceOf(TimeoutException.class);

        verify(client).incr("my-key", 2);
    }

    @Test
    public void whenIncrWithInterrupted_thenThrowException() throws InterruptedException, MemcachedException, TimeoutException {
        given(client.incr(anyString(), anyLong())).willThrow(new InterruptedException("Test interrupted error"));

        assertThatThrownBy(() -> memcachedClient.incr("my-key", 2))
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to increment key")
                .hasCauseInstanceOf(InterruptedException.class);

        verify(client).incr("my-key", 2);
    }

    @Test
    public void whenShutdown_thenCorrectMethodInvoked() throws IOException {
        memcachedClient.shutdown();

        verify(client).shutdown();
    }

    @Test
    public void whenShutdownWithError_thenThrowException() throws IOException {
        doThrow(IOException.class).when(client).shutdown();

        assertThatThrownBy(() -> memcachedClient.shutdown())
                .isInstanceOf(MemcachedOperationException.class)
                .hasMessage("Failed to shutdown client")
                .hasCauseInstanceOf(IOException.class);

        verify(client).shutdown();
    }

}