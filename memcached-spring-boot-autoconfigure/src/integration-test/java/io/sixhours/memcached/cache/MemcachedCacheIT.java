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

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Memcached cache integration tests.
 *
 * @author Igor Bolic
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MemcachedCacheIT.CacheConfig.class)
public class MemcachedCacheIT {

    @ClassRule
    public static GenericContainer memcached = new GenericContainer("memcached:alpine")
            .withExposedPorts(11211);

    @Autowired
    MemcachedCacheManager cacheManager;

    @Autowired
    IMemcachedClient memcachedClient;

    @Autowired
    AuthorService authorService;

    @Autowired
    BookService bookService;

    @Autowired
    @Qualifier("memcachedCacheMeterBinderProvider")
    CacheMeterBinderProvider provider;

    @Before
    public void setUp() {
        memcachedClient.flush();
        bookService.resetCounters();
        authorService.resetCounters();
    }

    @Test
    public void whenFindAllThenBooksCached() {
        List<Book> books = bookService.findAll();

        assertThat(books).isNotNull();
        assertThat(bookService.getCounterFindAll()).isEqualTo(1);

        bookService.findAll();
        bookService.findAll();
        assertThat(bookService.getCounterFindAll()).isEqualTo(1);

        Cache booksCache = cacheManager.getCache("books");
        Object value = booksCache.get(SimpleKey.EMPTY);

        assertThat(value).isNotNull();
    }

    @Test
    public void whenFindAllThenAuthorsCached() {
        List<Author> authors = authorService.findAll();

        assertThat(authors).isNotNull();
        assertThat(authorService.getCounterFindAll()).isEqualTo(1);

        authorService.findAll();
        authorService.findAll();
        assertThat(authorService.getCounterFindAll()).isEqualTo(1);

        Object value = cacheManager.getCache("authors").get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();
    }

    @Test
    public void whenTimeoutThenAuthorCacheExpired() throws InterruptedException {
        List<Author> authors = authorService.findAll();

        assertThat(authors).isNotNull();
        assertThat(authorService.getCounterFindAll()).isEqualTo(1); // first time read from DB

        authorService.findAll();
        assertThat(authorService.getCounterFindAll()).isEqualTo(1);

        Thread.sleep(1000 * 5L);

        Object value = cacheManager.getCache("authors").get(SimpleKey.EMPTY);
        assertThat(value).isNull();

        authors = authorService.findAll();

        assertThat(authors).isNotNull();
        assertThat(authorService.getCounterFindAll()).isEqualTo(2); // second time read from DB

        value = cacheManager.getCache("authors").get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();
    }

    @Test
    public void whenFindByYearThenBooksWithYearNotCached() {
        List<Book> books = bookService.findByYear(2016);

        assertThat(books).isNotNull();
        assertThat(bookService.getCounterFindByYear()).isEqualTo(1);

        bookService.findByYear(2016);
        bookService.findByYear(2016);
        assertThat(bookService.getCounterFindByYear()).isEqualTo(3);

        Object value = cacheManager.getCache("books").get("2016");
        assertThat(value).isNull();
    }

    @Test
    public void whenFindByTitleThenBookWithTitleCached() {
        Book book = bookService.findByTitle("Kotlin");

        assertThat(book).isNotNull();
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        Book cachedBook = bookService.findByTitle("Kotlin");
        assertThat(cachedBook).isNotNull();
        assertThat(cachedBook).isNotSameAs(book);
        assertThat(cachedBook).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        Object value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void whenTimeoutThenBookCacheExpired() throws InterruptedException {
        Book book = bookService.findByTitle("Kotlin");

        assertThat(book).isNotNull();
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        bookService.findByTitle("Kotlin");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        Thread.sleep(1000 * 7L);

        Object value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNull();

        Book book2 = bookService.findByTitle("Kotlin");
        assertThat(book2).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(2);

        value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void whenTimeoutAndNewValueAddedThenBookCacheNotExpired() throws InterruptedException {
        Book kotlin = bookService.findByTitle("Kotlin");

        assertThat(kotlin).isNotNull();
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        bookService.findByTitle("Kotlin");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        Thread.sleep(1000 * 3L);

        Book spring = bookService.findByTitle("Spring Boot in Action");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(2);

        bookService.findByTitle("Spring Boot in Action");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(2);

        Thread.sleep(1000 * 3L);

        // first value expired
        Object value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNull();

        // second value did not expire
        value = cacheManager.getCache("books").get("Spring Boot in Action");
        assertThat(value).isNotNull();

        Book expectKotlin = bookService.findByTitle("Kotlin");
        assertThat(expectKotlin).isEqualTo(kotlin);
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(3);

        Book expectSpring = bookService.findByTitle("Spring Boot in Action");
        assertThat(expectSpring).isEqualTo(spring);
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(3);

        value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNotNull();

        value = cacheManager.getCache("books").get("Spring Boot in Action");
        assertThat(value).isNotNull();
    }

    @Test
    public void whenUnlessNotMetThenBookWithTitleAndYearCached() {
        Book book = bookService.findByTitleAndYear("Programming Kotlin", 2017);
        Book cachedBook = bookService.findByTitleAndYear("Programming Kotlin", 2017);

        assertThat(book).isNotNull();
        assertThat(cachedBook).isNotNull();
        assertThat(cachedBook).isNotSameAs(book);
        assertThat(cachedBook).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitleAndYear()).isEqualTo(1);

        Object value = cacheManager.getCache("books").get("Programming Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void whenUnlessMetThenBookWithTitleAndYearNotCached() {
        Book book = bookService.findByTitleAndYear("Spring Boot in Action", 2016);
        Book cachedBook = bookService.findByTitleAndYear("Spring Boot in Action", 2016);

        assertThat(book).isNotNull();
        assertThat(cachedBook).isNotNull();
        assertThat(cachedBook).isSameAs(book);
        assertThat(cachedBook).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitleAndYear()).isEqualTo(2);

        Object value = cacheManager.getCache("books").get("Spring Boot in Action");
        assertThat(value).isNull();
    }

    @Test
    public void whenUpdateThenBookWithTitleEvicted() {
        Book book = bookService.findByTitle("Spring Boot in Action");
        assertThat(book).isNotNull();

        bookService.findByTitle("Spring Boot in Action");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        bookService.findByTitle("Kotlin");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(2);

        bookService.update(new Book(1, "Spring Boot in Action", 2016));

        Object value = cacheManager.getCache("books").get("Spring Boot in Action");
        assertThat(value).isNull();
        value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void whenClearThenOnlyBooksEvicted() {
        bookService.findAll();
        authorService.findAll();

        bookService.clear();

        Object value = cacheManager.getCache("books").get(SimpleKey.EMPTY);
        assertThat(value).isNull();
        value = cacheManager.getCache("authors").get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void whenGettingBooksFromCacheThenReturnCorrectStatistics() {
        bookService.findAll();
        bookService.findAll();
        bookService.findAll();

        bookService.findByTitle("Spring Boot in Action");
        bookService.findByTitle("Spring Boot in Action");
        bookService.findByTitle("Kotlin");
        bookService.findByTitle("Kotlin");
        bookService.findByTitle("Kotlin");

        Cache books = cacheManager.getCache("books");

        Tags expectedTag = Tags.of("app", "test");
        MeterBinder metrics = provider.getMeterBinder(books, expectedTag);

        MeterRegistry registry = new SimpleMeterRegistry();
        metrics.bindTo(registry);

        FunctionCounter hits = registry.get("cache.gets").tags(expectedTag).tag("result", "hit").functionCounter();
        FunctionCounter misses = registry.get("cache.gets").tags(expectedTag).tag("result", "miss").functionCounter();

        assertThat(hits.count()).isEqualTo(5);
        assertThat(misses.count()).isEqualTo(3);

        bookService.findAll();
        bookService.findByTitle("Kotlin");

        assertThat(hits.count()).isEqualTo(7);
        assertThat(misses.count()).isEqualTo(3);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCaching
    @ComponentScan(basePackageClasses = {AuthorService.class, BookService.class})
    static class CacheConfig {

        @Bean
        public MemcachedCacheManager cacheManager() throws IOException {
            final MemcachedCacheManager memcachedCacheManager = new MemcachedCacheManager(memcachedClient());
            memcachedCacheManager.setExpiration(5);
            memcachedCacheManager.setExpirationPerCache(Collections.singletonMap("authors", 3));

            return memcachedCacheManager;
        }

        @Bean
        public IMemcachedClient memcachedClient() throws IOException {
            final String host = memcached.getContainerIpAddress();
            final int port = memcached.getMappedPort(11211);

            return new XMemcachedClient(new XMemcachedClientBuilder(Collections.singletonList(new InetSocketAddress(host, port))).build());
        }
    }
}
