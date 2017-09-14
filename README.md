[![Build Status](https://travis-ci.org/igorbolic/memcached-spring-boot.svg?branch=master)](https://travis-ci.org/igorbolic/memcached-spring-boot) 
[![codecov](https://codecov.io/gh/igorbolic/memcached-spring-boot/branch/master/graph/badge.svg)](https://codecov.io/gh/igorbolic/memcached-spring-boot)
[![Join the chat at gitter.im/six-hours/memcached-spring-boot](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/six-hours/memcached-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Memcached Spring Boot

Library that provides support for auto-configuration of Memcached cache in a Spring Boot application.
The auto-configuration will be triggered if the `spymemcached` client is found on the classpath. More 
specific, it is using the `spymemcached` fork: [Amazon's ElastiCache Clustered Client](https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java).

## Properties

Properties can be set in your `application.properties/application.yml` file or as a command line properties. Below is the
full list of supported properties:

```.properties
# MEMCACHED CACHE 
memcached.cache.servers: # Comma-separated list of hostname:port for memcached servers (default "localhost:11211")
memcached.cache.mode: # Memcached client mode (use one of following: "static", "dynamic"). Default mode is "static", use "dynamic" for AWS node auto discovery
memcached.cache.expiration: # Cache expiration in seconds (default "60")
memcached.cache.prefix: # Cache key prefix (default "memcached:spring-boot")
memcached.cache.namespace: # Cache eviction namespace key name (default "namespace")
```

All of the values have sensible defaults and bound to [MemcachedCacheProperties](https://github.com/igorbolic/memcached-spring-boot/blob/master/memcached-spring-boot-autoconfigure/src/main/java/io/sixhours/memcached/cache/MemcachedCacheProperties.java) class. 
It is advised to set your own `namespace` and `prefix` values to avoid cache data conflicts when multiple applications are sharing the same Memcached server.

## Usage

To plug-in Memcached cache in your application follow the steps below:

1. Add library to your dependency management section:
   * **Gradle**
   
      ```groovy
      compile('io.sixhours:memcached-spring-boot-starter:1.0.0') 
      ```
   * **Maven**
   
      ```xml
      <dependency>
          <groupId>io.sixhours</groupId>
          <artifactId>memcached-spring-boot-starter</artifactId>
          <version>1.0.0</version>
      </dependency>
      ```
      
2. Configure location of your `Memcached` key-value store in the properties file (e.g. `application.yml`) e.g.
   
    ```.properties
    memcached.cache:
        servers: example.com:11211
        mode: dynamic
        expiration: 86400
    ```
   For multi-server configuration specify comma-separated list of hostname:port with `static` mode:
   
    ```.properties
     memcached.cache:
       servers: example1.com:11211,example2.com:11211
       mode: static
       expiration: 86400
     ```
   
3. Enable caching annotations by adding Spring's `@EnableCaching` annotation to one of your `@Configuration` classes e.g.
    
    ```java
    @Configuration
    @EnableCaching
    public class CacheConfiguration {
    }
    ```

    Now you can add caching to an operation of your service as in an example:  
 
    ```java
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.stereotype.Component;
    
    @Component
    public class BookService {
    
        @Cacheable("books")
        public Book findByTitle(String title) {
            // ...
        }
    
    }
    ```
