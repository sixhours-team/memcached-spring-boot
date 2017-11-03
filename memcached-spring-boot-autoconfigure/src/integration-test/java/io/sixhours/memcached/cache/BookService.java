package io.sixhours.memcached.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Bolic
 */
@Repository
public class BookService {

    private List<Book> books = Arrays.asList(
            new Book(1, "Kotlin in Action", 2017),
            new Book(2, "Spring Boot in Action", 2016),
            new Book(3, "Programming Kotlin", 2017),
            new Book(4, "Kotlin", 2017));

    private int counterFindAll = 0;
    private int counterFindByTitle = 0;
    private int counterFindByTitleWithYear = 0;

    public List<Book> findAll() {
        counterFindAll++;
        return this.books;
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
    public Book findByTitleWithYear(String title, int year) {
        counterFindByTitleWithYear++;
        return books.stream()
                .filter(b -> b.getTitle().equals(title) && b.getYear().equals(year))
                .collect(Collectors.reducing((a, b) -> null))
                .get();
//        return new Book(5, title, year);
    }

    @CacheEvict(value = "books", key = "#book.title")
    public void update(Book book) {
        // do nothing
    }

    public void resetCounters() {
        counterFindAll = 0;
        counterFindByTitle = 0;
        counterFindByTitleWithYear = 0;
    }

    public int getCounterFindAll() {
        return counterFindAll;
    }

    public int getCounterFindByTitle() {
        return counterFindByTitle;
    }

    public int getCounterFindByTitleWithYear() {
        return counterFindByTitleWithYear;
    }


}
