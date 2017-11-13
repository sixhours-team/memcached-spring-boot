/*
 * Copyright 2017 Sixhours.
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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookService {

    private final List<Book> books = Arrays.asList(
            new Book(1, "Kotlin in Action", 2017),
            new Book(2, "Spring Boot in Action", 2016),
            new Book(3, "Programming Kotlin", 2017),
            new Book(4, "Kotlin", 2017));

    private int counterFindAll = 0;
    private int counterFindByYear = 0;
    private int counterFindByTitle = 0;
    private int counterFindByTitleAndYear = 0;

    @Cacheable("books")
    public List<Book> findAll() {
        counterFindAll++;
        return this.books;
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
                .reduce((a, b) -> null)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Cacheable(value = "books", key = "#title", unless = "#year <= 2016")
    public Book findByTitleAndYear(String title, int year) {
        counterFindByTitleAndYear++;
        return books.stream()
                .filter(b -> b.getTitle().equals(title) && b.getYear().equals(year))
                .reduce((a, b) -> null)
                .orElseThrow(IllegalArgumentException::new);
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
        counterFindByTitleAndYear = 0;
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

    public int getCounterFindByTitleAndYear() {
        return counterFindByTitleAndYear;
    }

}
