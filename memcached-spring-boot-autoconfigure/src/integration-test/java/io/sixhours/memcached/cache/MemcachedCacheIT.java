package io.sixhours.memcached.cache;

import net.spy.memcached.ClientMode;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MemcachedCacheIT.TestConfig.class)
public class MemcachedCacheIT {

    @ClassRule
    public static GenericContainer memcached = new GenericContainer("memcached:alpine")
            .withExposedPorts(11211);

    @Autowired
    MemcachedCacheManager cacheManager;

    @Autowired
    MemcachedClient memcachedClient;

    @Autowired
    BookService bookService;

    @Before
    public void setUp() {
        memcachedClient.flush();
        bookService.resetCounters();
    }

    @Test
    public void thatBooksAreNotCached() {
        List<Book> data = bookService.findAll();

        assertThat(data).isNotNull();
        assertThat(bookService.getCounterFindAll()).isEqualTo(1);

        bookService.findAll();
        bookService.findAll();
        assertThat(bookService.getCounterFindAll()).isEqualTo(3);
    }

    @Test
    public void thatBookHasBeenCached() {
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
    public void thatBookCacheHasExpired() throws InterruptedException {
        Book book = bookService.findByTitle("Kotlin");

        assertThat(book).isNotNull();
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        bookService.findByTitle("Kotlin");
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(1);

        Thread.sleep(1000 * 6L);
        Object value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNull();

        Book book2 = bookService.findByTitle("Kotlin");
        assertThat(book2).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitle()).isEqualTo(2);

        value = cacheManager.getCache("books").get("Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void thatBookHasBeenCachedWhenUnlessNotMet() {
        Book book = bookService.findByTitleWithYear("Programming Kotlin", 2017);
        Book cachedBook = bookService.findByTitleWithYear("Programming Kotlin", 2017);

        assertThat(book).isNotNull();
        assertThat(cachedBook).isNotNull();
        assertThat(cachedBook).isNotSameAs(book);
        assertThat(cachedBook).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitleWithYear()).isEqualTo(1);

        Object value = cacheManager.getCache("books").get("Programming Kotlin");
        assertThat(value).isNotNull();
    }

    @Test
    public void thatBookHasBeenNotBeenCachedWhenUnlessMet() {
        Book book = bookService.findByTitleWithYear("Spring Boot in Action", 2016);
        Book cachedBook = bookService.findByTitleWithYear("Spring Boot in Action", 2016);

        assertThat(book).isNotNull();
        assertThat(cachedBook).isNotNull();
        assertThat(cachedBook).isSameAs(book);
        assertThat(cachedBook).isEqualTo(book);
        assertThat(bookService.getCounterFindByTitleWithYear()).isEqualTo(2);

        Object value = cacheManager.getCache("books").get("Spring Boot in Action");
        assertThat(value).isNull();
    }

    @Test
    public void thatBookWithTitleKeyHasBeenEvicted() {
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

    @Configuration
    @EnableCaching
    @ComponentScan(basePackageClasses = BookService.class)
    static class TestConfig {

        @Bean
        public MemcachedCacheManager cacheManager() throws IOException {
            final MemcachedCacheManager memcachedCacheManager = new MemcachedCacheManager(memcachedClient());
            memcachedCacheManager.setExpiration(5);

            return memcachedCacheManager;
        }

        @Bean
        public MemcachedClient memcachedClient() throws IOException {
            final String host = memcached.getContainerIpAddress();
            final int port = memcached.getMappedPort(11211);

            return new MemcachedClient(new DefaultConnectionFactory(ClientMode.Static),
                    Collections.singletonList(new InetSocketAddress(host, port)));
        }
    }
}
