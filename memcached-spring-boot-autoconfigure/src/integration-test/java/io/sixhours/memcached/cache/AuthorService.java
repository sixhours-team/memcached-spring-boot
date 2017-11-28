package io.sixhours.memcached.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class AuthorService {

    @Cacheable(cacheNames = "authors")
    public List<Author> findAll() {
        return Arrays.asList(new Author("John"), new Author("David"));
    }
}
