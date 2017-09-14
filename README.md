[![Build Status](https://travis-ci.org/igorbolic/memcached-spring-boot.svg?branch=master)](https://travis-ci.org/igorbolic/memcached-spring-boot) 
[![codecov](https://codecov.io/gh/igorbolic/memcached-spring-boot/branch/master/graph/badge.svg)](https://codecov.io/gh/igorbolic/memcached-spring-boot)
[![Join the chat at gitter.im/six-hours/memcached-spring-boot](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/six-hours/memcached-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Memcached Spring Boot

Library that provides support for auto-configuration of Memcached cache in a Spring Boot application.

It provides implementation for the [Spring Cache Abstraction](https://docs.spring.io/spring/docs/4.3.x/spring-framework-reference/html/cache.html), backed by the [Amazon's ElastiCache Clustered Client](https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java).
Supports cache eviction per key, as well as clearing out of the entire cache region.

## Properties

Properties can be set in your `application.properties/application.yml` file or as a command line properties. Below is the
full list of supported properties:

```.yaml
# MEMCACHED CACHE 
memcached.cache.servers: # Comma-separated list of hostname:port for memcached servers (default "localhost:11211")
memcached.cache.mode: # Memcached client mode (use one of following: "static", "dynamic"). Default mode is "static", use "dynamic" for AWS node auto discovery
memcached.cache.expiration: # Cache expiration in seconds (default "60")
memcached.cache.prefix: # Cache key prefix (default "memcached:spring-boot")
memcached.cache.namespace: # Cache eviction namespace key name (default "namespace")
```

All of the values have sensible defaults and are bound to [MemcachedCacheProperties](https://github.com/igorbolic/memcached-spring-boot/blob/master/memcached-spring-boot-autoconfigure/src/main/java/io/sixhours/memcached/cache/MemcachedCacheProperties.java) class.

**Notice:** 
>If multiple applications are sharing the same Memcached server, make sure to specify unique cache `prefix` for each application 
in order to avoid cache data conflicts.

## Usage

To plug-in Memcached cache in your application follow the steps below:

1. Include library as a Gradle or Maven compile dependency:
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
      
2. Configure `Memcached` key-value store in your properties file (`application.yml`).

    **Example**

    To manually connect to one or more cache servers (nodes), specify comma-separated list of hostname:port with the `static` mode:
       
    ```.properties
     memcached.cache:
       servers: example1.com:11211,example2.com:11211
       mode: static
       expiration: 86400
     ```

    To connect to a cluster with AWS [Auto Discovery](http://docs.aws.amazon.com/AmazonElastiCache/latest/UserGuide/AutoDiscovery.html), specify
    cluster configuration endpoint in `memcached.cache.servers` property with the `dynamic` mode:
   
    ```.properties
    memcached.cache:
        servers: mycluster.example.com:11211
        mode: dynamic
        expiration: 86400
    ```
   
3. Enable caching support by adding `@EnableCaching` annotation to one of your `@Configuration` classes.

    ```java
    import org.springframework.cache.annotation.EnableCaching;
    import org.springframework.context.annotation.Configuration;   
 
    @Configuration
    @EnableCaching
    public class CacheConfiguration {
    }
    ```

    Now you can add caching to an operation of your service, for example:
 
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

For further details on using the Memcached cache in a Spring Boot application please look at the [demo](https://github.com/igorbolic/spring-boot-memcached-demo) project. 
