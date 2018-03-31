package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.query.KeywordSearcher;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ehcache.expiry.ExpiryPolicy;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A cache for search queries.
 */
public class QueryCache implements Closeable {

  private static Logger logger = LoggerFactory.getLogger(QueryCache.class);

  private final CacheManager cacheManager;

  private final Cache<String, BigInteger> cache;

  private final CacheEventListener<String, BigInteger> removeListener;

  /**
   * Creates a new QueryCache object.
   * @param cacheSize The size of the cache.
   * @param removeListener A listener that will be informed whenever an item of the caches gets removed.
   */
  public QueryCache(int cacheSize, CacheEventListener<String, BigInteger> removeListener) {

    CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
        .newEventListenerConfiguration(new RemoveListener(),
            EventType.EXPIRED,
            EventType.EVICTED,
            EventType.REMOVED,
            EventType.UPDATED)
        .unordered().asynchronous();

    cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    cache = cacheManager.createCache("cache",
        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
                                                                BigInteger.class,
                                                                ResourcePoolsBuilder.heap(cacheSize))
            .add(cacheEventListenerConfiguration)
            //.withExpiry(ExpiryPolicy.NO_EXPIRY)
            .withExpiry(new ExpiryPolicy<String, BigInteger>() {
              @Override
              public Duration getExpiryForCreation(String key, BigInteger value) {
                return Duration.ofMinutes(10);
              }

              @Override
              public Duration getExpiryForAccess(String key, Supplier<? extends BigInteger> value) {
                return Duration.ofMinutes(10);
              }

              @Override
              public Duration getExpiryForUpdate(String key, Supplier<? extends BigInteger> oldValue, BigInteger newValue) {
                return Duration.ofMinutes(10);
              }
            })
            .build());


    this.removeListener = removeListener;
  }

  /**
   * Adds a keyword search query to the cache.
   * @param keywords The keywords
   * @param operator The used operator.
   * @param resultID The ID of the repository where the query result is stored.
   */
  public void add(List<String> keywords, KeywordSearcher.Operator operator, BigInteger resultID) {
    String key = buildKey(keywords, operator);
    cache.put(key, resultID);
  }

  /**
   * Provides the ID of a query result repository for a keyword search query.
   * @param keywords The keywords
   * @param operator The used operator.
   * @return The ID of the query result repository or null if the query is not cached.
   */
  public BigInteger get(List<String> keywords, KeywordSearcher.Operator operator) {
    String key = buildKey(keywords, operator);
    return cache.get(key);
  }

  /**
   * Deletes all cache elements.
   */
  public void clear() {
    cache.forEach( elem -> {
      cache.remove(elem.getKey());
    });
  }

  @Override
  public void close() throws IOException {

    //clean up cache
    // cannot be done this way as play framework is shutting down
    // -> data collector doesn't accept connections anymore
    /*cache.forEach(entry->{
      cache.remove(entry.getKey());
    });*/

    cacheManager.close();
  }

  private String buildKey(List<String> keywords, KeywordSearcher.Operator operator) {
    String key =  keywords.stream()
        .map(String::toLowerCase) // we want uniform keys
        .map(String::trim)  // we want no whitespaces to interfere
        .distinct() // duplicate keywords are ignored
        .sorted(String::compareTo) // sort so that equal keys are represented equal
        .collect(Collectors.joining(",")); // keys are concatenated keywords, delimited by ','

    String operatorStr = "and";
    if (operator == KeywordSearcher.Operator.OR)
      operatorStr = "or";

    return key + "&" + operatorStr + "&";
  }

  /**
   * A remove listener that executes the registered remove listener when a cache item gets removed.
   */
  private class RemoveListener implements CacheEventListener<String, BigInteger> {

    @Override
    public void onEvent(CacheEvent<? extends String, ? extends BigInteger> event) {

      EventType type = event.getType();

      logger.debug("RemoveListener::onEvent called; event type = " + type);

      if (type == EventType.UPDATED) {
        logger.error("Cached queries shouldn't be updated/replaced!");
      }

      if (event.getOldValue() != null) {
        logger.info("Delete stored query result...");
      }

      if (removeListener == null) return;

      removeListener.onEvent(event);
    }
  }
}