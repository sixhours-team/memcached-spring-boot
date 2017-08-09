[![Build Status](https://travis-ci.org/igorbolic/memcached-spring-boot.svg?branch=master)](https://travis-ci.org/igorbolic/memcached-spring-boot) 
[![codecov](https://codecov.io/gh/igorbolic/memcached-spring-boot/branch/master/graph/badge.svg)](https://codecov.io/gh/igorbolic/memcached-spring-boot)

# Memcached Spring Boot Auto-configuration

Library that provides support for auto-configuration of Memcached cache in a Spring Boot application.
The auto-configuration will be triggered if the `spymemcached` client is found on the classpath. More 
specific, it is using the `spymemcached` fork: [Amazon's ElastiCache Clustered Client](https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java).



## Installation

To install library in your local Maven repository run this command:

`./gradlew clean test install`



## Properties

Properties can be set in your `application.properties/application.yml` file or as a command line properties. Below is the
full list of supported properties for the Memcached Cache library:

```.properties
# MEMCACHED CACHE 
memcached.cache.host: # Memcached server host (default "localhost")
memcached.cache.port: # Memcached server port (default "11211")
memcached.cache.mode: # Memcached client mode (use one of following: "static", "dynamic"). Default mode is "static", use "dynamic" for AWS node auto discovery
memcached.cache.namespace: # Cache eviction namespace key name (default "namespace")
memcached.cache.expiration: # Cache expiration in seconds (default "60")
memcached.cache.prefix: # Cache key prefix (default "memcached:spring-boot")
```

All of the values have sensible defaults, and bound to [MemcachedCacheProperties](https://github.com/igorbolic/memcached-spring-boot/blob/master/memcached-spring-boot-autoconfigure/src/main/java/io/sixhours/memcached/cache/MemcachedCacheProperties.java) class. 
It is advised to set your own `namespace` and `prefix` values to avoid cache data conflicts when multiple applications are sharing the same Memcached server.


## Usage

To plug-in Memcached cache in your application follow the steps below:

1. Add libraries to your dependency management section:
   * **Gradle**
   
      ```shell
      compile('io.sixhours.memcached.cache:memcached-spring-boot-starter:0.0.1-SNAPSHOT') 
      ```
   * **Maven**
   
      ```xml
      <dependency>
          <groupId>io.sixhours.memcached.cache</groupId>
          <artifactId>memcached-spring-boot-starter</artifactId>
          <version>0.0.1-SNAPSHOT</version>
      </dependency>
      ```
      
2. Configure location of your `Memcached` key-value store in the properties file (e.g. `application.yml`) e.g.
   
      ```.properties
      memcached.cache:
        host: localhost
        port: 11211
        mode: dynamic
        expiration: 86400
      ```
   
3. Enable caching annotations by adding Spring's `@EnableCaching` annotation to one of your `@Configuration` classes e.g.
    
    ```java
    /**
     * Cache configuration.
     */
    @Configuration
    @EnableCaching
    public class CacheConfiguration {
    }
    ```

The Memcached cache store will be auto-configured on the application startup.
