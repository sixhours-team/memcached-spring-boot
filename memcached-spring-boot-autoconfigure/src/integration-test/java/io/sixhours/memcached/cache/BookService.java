package io.sixhours.memcached.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookService {

    private List<Book> books = Arrays.asList(
            new Book(1, "Kotlin in Action", 2017),
            new Book(2, "Spring Boot in Action", 2016),
            new Book(3, "Programming Kotlin", 2017),
            new Book(4, "Kotlin", 2017));

    private int counterFindAll = 0;
    private int counterFindByYear = 0;
    private int counterFindByTitle = 0;
    private int counterFindByTitleWithYear = 0;

    @Cacheable("books")
    public List<Book> findAll() {
        counterFindAll++;
        return this.books;
    }

    @Cacheable("books")
    public Book find(String argument, int year) {
        return new Book(6, "Test", 2017);
    }

    public List<Book> findByYear(int year) {
        counterFindByYear++;
        return books.stream()
                .filter(b -> b.getYear().equals(year))
                .collect(Collectors.toList());
    }

    @Cacheable("books")
    public Book findByTitle(String title) {
        counterFindByTitle++;
        return books.stream()
                .filter(b -> b.getTitle().equals(title))
                .collect(Collectors.reducing((a, b) -> null))
                .get();
    }

    @Cacheable(value = "books", key = "#title", unless = "#year <= 2016")
    public Book findByTitleAndYear(String title, int year) {
        counterFindByTitleWithYear++;
        return books.stream()
                .filter(b -> b.getTitle().equals(title) && b.getYear().equals(year))
                .collect(Collectors.reducing((a, b) -> null))
                .get();
    }

    @CacheEvict(value = "books", key = "#book.title")
    public void update(Book book) {
        // do nothing
    }

    @CacheEvict(value = "books", allEntries = true, beforeInvocation = true)
    @Cacheable(value = "books", key = "T(org.springframework.cache.interceptor.SimpleKey).EMPTY")
    public List<Book> deleteAndReCache(String title) {
        // delete book with title & return
        final List<Book> result = new ArrayList<>(books);
        result.removeIf(b -> b.getTitle().equals(title));

        return result;
    }

    public void resetCounters() {
        counterFindAll = 0;
        counterFindByYear = 0;
        counterFindByTitle = 0;
        counterFindByTitleWithYear = 0;
    }

    public int getCounterFindAll() {
        return counterFindAll;
    }

    public int getCounterFindByYear() {
        return counterFindByYear;
    }

    public int getCounterFindByTitle() {
        return counterFindByTitle;
    }

    public int getCounterFindByTitleWithYear() {
        return counterFindByTitleWithYear;
    }

}
