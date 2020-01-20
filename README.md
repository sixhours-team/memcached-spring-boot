[![Build Status](https://travis-ci.org/sixhours-team/memcached-spring-boot.svg?branch=master)](https://travis-ci.org/sixhours-team/memcached-spring-boot)
[![codecov](https://codecov.io/gh/sixhours-team/memcached-spring-boot/branch/master/graph/badge.svg)](https://codecov.io/gh/sixhours-team/memcached-spring-boot)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=io.sixhours%3Amemcached-spring-boot-autoconfigure&metric=alert_status)](https://sonarcloud.io/dashboard/index/io.sixhours%3Amemcached-spring-boot-autoconfigure)
[![Join the chat at gitter.im/six-hours/memcached-spring-boot](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/six-hours/memcached-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Memcached Spring Boot

Library that provides support for auto-configuration of Memcached cache in a Spring Boot application.

It provides implementation for the [Spring Cache Abstraction](https://docs.spring.io/spring/docs/4.3.x/spring-framework-reference/html/cache.html), backed by the [Amazon's ElastiCache Clustered Client](https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java).
Supports cache eviction per key, as well as clearing out of the entire cache region. Binaries are available from **Maven Central** and **JCenter**.

## Usage

To plug-in Memcached cache in your application follow the steps below:

1. Include library as a Gradle or Maven compile dependency:
   * **Gradle**

      ```groovy
      compile('io.sixhours:memcached-spring-boot-starter:1.3.2')
      ```
   * **Maven**

      ```xml
      <dependency>
          <groupId>io.sixhours</groupId>
          <artifactId>memcached-spring-boot-starter</artifactId>
          <version>1.3.2</version>
      </dependency>
      ```
2. Configure `Memcached` key-value store in your properties file (`application.yml`).

    **Example**

    To manually connect to one or more cache servers (nodes), specify comma-separated list of hostname:port with the `static` mode:

    ```yaml
     memcached.cache:
       servers: example1.com:11211,example2.com:11211
       mode: static
       # default expiration is '1d' ('86400' seconds) and custom ones for cache_name1 and cache_name2
       expiration: 1d 
       expiration-per-cache:
         cache_name1: 1h
         cache_name2: 30h
     ```

    To connect to a cluster with AWS [Auto Discovery](http://docs.aws.amazon.com/AmazonElastiCache/latest/UserGuide/AutoDiscovery.html), specify
    cluster configuration endpoint in `memcached.cache.servers` property with the `dynamic` mode:

    ```yaml
    memcached.cache:
        servers: mycluster.example.com:11211
        mode: dynamic
        expiration: 86400 # default expiration set to '86400' seconds i.e. 1 day
    ```

3. Enable caching support by adding `@EnableCaching` annotation to one of your `@Configuration` classes.

    ```java
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.cache.annotation.EnableCaching;

    @SpringBootApplication
    @EnableCaching
    public class Application {

    	public static void main(String[] args) {
    		SpringApplication.run(Application.class, args);
    	}
    }
    ```

    Now you can add caching to an operation of your service:

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

For further details on using the Memcached cache in a Spring Boot application please look at the [demo](https://github.com/sixhours-team/spring-boot-memcached-demo-java) project.

## Properties

Properties can be set in your `application.yml` file or as a command line properties. Below is the
full list of supported properties:

```yaml
# MEMCACHED CACHE
memcached.cache.servers: # Comma-separated list of hostname:port for memcached servers (default "localhost:11211")
memcached.cache.mode: # Memcached client mode (use one of following: "static", "dynamic"). Default mode is "static", use "dynamic" for AWS node auto discovery
memcached.cache.expiration: # Default cache expiration if not configured per cache (default "0", meaning that cache will never expire). If unit not specified, seconds will be used.
memcached.cache.expiration-per-cache.cacheName: # To set expiration value for cache named "cacheName" {cache_name}:{number} e.g. "authors: 3600" or "authors: 1h". If unit not specified, seconds will be used.
memcached.cache.prefix: # Cache key prefix (default "memcached:spring-boot")
memcached.cache.protocol: # Memcached client protocol. Supports "text" and "binary" protocols (default is "text" protocol)
memcached.cache.operation-timeout: # Memcached client operation timeout (default "2500 milliseconds"). If unit not specified, milliseconds will be used.
```

All of the values have sensible defaults and are bound to [MemcachedCacheProperties](https://github.com/sixhours-team/memcached-spring-boot/blob/master/memcached-spring-boot-autoconfigure/src/main/java/io/sixhours/memcached/cache/MemcachedCacheProperties.java) class.

Duration properties such as `expiration` and `expiration-per-cache` by default are using unit of seconds if no unit is specified. For `operation-timeout` property unit of milliseconds is the default one.

E.g. to specify a `expiration` of 30 seconds, `30`, `PT30S` (ISO-8601 format) and `30s` are all equivalent. A `operatio-timeout` of 500ms can be specified in any of the following form: `500`, `PT0.5S` (ISO-8601 format) and `500ms`.

Supported units are:

* `ns` for nanoseconds

* `us` for microseconds

* `ms` for milliseconds

* `s` for seconds

* `m` for minutes

* `h` for hours

* `d` for days

**Notice:**
>If different applications are sharing the same Memcached server, make sure to specify unique cache `prefix` for each application
in order to avoid cache conflicts.

## Build

In order to build the project you will have to have [Java 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Docker](https://www.docker.com/get-docker) installed.
To build the project invoke the following command:

    ./gradlew clean build

To install the modules in the local Maven repository:

    ./gradlew clean build publishToMavenLocal

## License

Memcached Spring Boot is an Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
